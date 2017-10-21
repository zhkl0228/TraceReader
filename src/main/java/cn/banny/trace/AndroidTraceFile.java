package cn.banny.trace;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

class AndroidTraceFile implements TraceFile {

    private static final int kTraceMethodActionMask = 0x3;

    private static TraceRecord readRecord(DataInput input, Map<Integer, TraceThreadInfo> threadMap, Map<Integer, MethodSpec> methodMap, int version, int recordSize, ClockSource clockSource) throws IOException {
        try {
            int threadId;
            int methodId;
            int deltaTimeInUsec, wallTimeInUsec;

            if (version == 1) {
                threadId = input.readByte();
                recordSize -= 1;
            } else {
                threadId = Short.reverseBytes(input.readShort()) & 0xffff;
                recordSize -= 2;
            }

            methodId = Integer.reverseBytes(input.readInt());
            recordSize -= 4;

            switch (clockSource) {
                case WALL:
                    deltaTimeInUsec = 0;
                    wallTimeInUsec = Integer.reverseBytes(input.readInt());
                    recordSize -= 4;
                    break;
                case DUAL:
                    deltaTimeInUsec = Integer.reverseBytes(input.readInt());
                    wallTimeInUsec = Integer.reverseBytes(input.readInt());
                    recordSize -= 8;
                    break;
                default:
                case THREAD_CPU:
                    deltaTimeInUsec = Integer.reverseBytes(input.readInt());
                    wallTimeInUsec = 0;
                    recordSize -= 4;
                    break;
            }

            if (recordSize > 0) {
                input.skipBytes(recordSize);
            }

            TraceThreadInfo threadInfo = threadMap.get(threadId);
            TraceRecord record = new TraceRecord(threadId, methodId & ~kTraceMethodActionMask, MethodAction.decodeAction(methodId & kTraceMethodActionMask), deltaTimeInUsec, threadInfo, methodMap);
            record.setWallTimeInUsec(wallTimeInUsec);
            return record;
        } catch (EOFException e) {
            return null;
        }
    }

    private synchronized void readMethodCallNodes(DataInput traceFile, Map<Integer, TraceThreadInfo> threadMap, Map<Integer, MethodSpec> methodMap, int version, int recordSize, ClockSource clockSource) throws IOException {
        while (true) {
            TraceRecord record = readRecord(traceFile, threadMap, methodMap, version, recordSize, clockSource);
            if (record == null) {
                break;
            }

            TraceThreadInfo threadInfo = record.getThreadInfo();
            if (threadInfo == null) {
                continue;
            }

            if (threadInfo.list == null || threadInfo.stack == null) {
                threadInfo.list = new ArrayList<>();
                threadInfo.stack = new Stack<>();

                threadInfo.stack.push(null);
            }

            if (record.getMethodAction() == MethodAction.METHOD_TRACE_ENTER) {
                if (!threadInfo.stack.isEmpty()) {
                    record.setParent(threadInfo.stack.peek());
                }
                threadInfo.stack.push(record);
            } else {
                if (threadInfo.stack.peek() == null) {
                    continue;
                }

                TraceRecord exit = threadInfo.stack.pop();
                if (exit.getMethodId() != record.getMethodId()) {
                    throw new IllegalStateException("exit method invalid: record=" + record + ", exit=" + exit);
                }

                threadInfo.lastExitDeltaTimeInUsec = record.getDeltaTimeInUsec();
                exit.setThreadTimeInUsec(record.getDeltaTimeInUsec() - exit.getDeltaTimeInUsec());
                if (threadInfo.stack.size() == 1) {
                    threadInfo.list.add(exit.toMethodCallNode());
                } else if (threadInfo.stack.isEmpty()) {
                    break;
                }
            }
        }

        for (TraceThreadInfo threadInfo : threadMap.values()) {
            if (threadInfo.list == null || threadInfo.stack == null) {
                continue;
            }

            while (threadInfo.stack.size() > 2) {
                threadInfo.stack.pop();
            }
            if (threadInfo.stack.size() == 2) {
                TraceRecord record = threadInfo.stack.pop();
                if (threadInfo.lastExitDeltaTimeInUsec > record.getDeltaTimeInUsec()) {
                    record.setThreadTimeInUsec(threadInfo.lastExitDeltaTimeInUsec - record.getDeltaTimeInUsec());
                }
                threadInfo.list.add(record.toMethodCallNode());
            }
        }
    }

    private static final int TRACE_MAGIC = 0x534c4f57;

    private static final int PARSE_VERSION = 0;
    private static final int PARSE_THREADS = 1;
    private static final int PARSE_METHODS = 2;
    private static final int PARSE_OPTIONS = 4;

    private final List<ThreadInfo> threadInfos;

    AndroidTraceFile(DataInput input) throws IOException {
        super();

        int versionNumber = -1;
        final Map<String, String> propertiesMap = new HashMap<>();

        final List<ThreadInfo> threadInfoList = new ArrayList<>();
        final Map<Integer, TraceThreadInfo> threadMap = new HashMap<>();

        final Map<Integer, MethodSpec> methodMap = new HashMap<>();

        ClockSource clockSource = null;

        int mode = PARSE_VERSION;
        while (true) {
            String line = input.readLine();
            if (line == null) {
                throw new IOException("Key section does not have an *end marker");
            }

            if (line.startsWith("*")) {
                if (line.equals("*version")) {
                    mode = PARSE_VERSION;
                    continue;
                }
                if (line.equals("*threads")) {
                    mode = PARSE_THREADS;
                    continue;
                }
                if (line.equals("*methods")) {
                    mode = PARSE_METHODS;
                    continue;
                }
                if (line.equals("*end")) {
                    break;
                }
            }
            switch (mode) {
                case PARSE_VERSION:
                    versionNumber = Integer.decode(line);
                    mode = PARSE_OPTIONS;
                    break;
                case PARSE_THREADS:
                    parseThread(line, threadInfoList, threadMap);
                    break;
                case PARSE_METHODS:
                    parseMethod(line, methodMap);
                    break;
                case PARSE_OPTIONS:
                    ClockSource clock = parseOption(line, propertiesMap);
                    if (clock != null) {
                        clockSource = clock;
                    }
                    break;
            }
        }

        Collections.sort(threadInfoList, new Comparator<ThreadInfo>() {
            @Override
            public int compare(ThreadInfo o1, ThreadInfo o2) {
                return o1.getThreadId() - o2.getThreadId();
            }
        });
        this.threadInfos = Collections.unmodifiableList(threadInfoList);

        if (clockSource == null) {
            clockSource = ClockSource.THREAD_CPU;
        }

        int magic = input.readInt();
        if (magic != TRACE_MAGIC) {
            throw new IOException("Error: magic number mismatch; got 0x" + Integer.toHexString(magic) + ", expected 0x" + Integer.toHexString(TRACE_MAGIC));
        }

        // read version
        int version = Short.reverseBytes(input.readShort()) & 0xffff;
        if (version != versionNumber) {
            throw new IOException("Error: version number mismatch; got " + version + " in data header but " + versionNumber + " in options");
        }
        if (version < 1 || version > 3) {
            throw new IOException("Error: unsupported trace version number " + version);
        }

        // read offset
        int offset = Short.reverseBytes(input.readShort()) & 0xffff;
        offset -= 8;

        // long startTimeInUsec = Long.reverseBytes(input.readLong()) & 0x7fffffffffffffffL;
        input.readLong(); // read startWhen
        offset -= 8;

        // read record size
        int recordSize;
        if (version == 1) {
            recordSize = 9;
        } else if (version == 2) {
            recordSize = 10;
        } else {
            recordSize = Short.reverseBytes(input.readShort()) & 0xffff;
            offset -= 2;
        }

        input.skipBytes(offset);
        readMethodCallNodes(input, threadMap, methodMap, version, recordSize, clockSource);
        for (TraceThreadInfo threadInfo : threadMap.values()) {
            threadInfo.setTop(threadInfo.getNodes());
        }
    }

    private void parseMethod(String line, Map<Integer, MethodSpec> methodMap) {
        String[] tokens = line.split("\t");
        int methodId = Long.decode(tokens[0]).intValue();
        String className = tokens[1];
        String methodName = null;
        String signature = null;
        String pathname = null;
        int lineNumber = -1;
        if (tokens.length == 6) {
            methodName = tokens[2];
            signature = tokens[3];
            pathname = tokens[4];
            lineNumber = Integer.decode(tokens[5]);
            pathname = constructPathname(className, pathname);
        } else if (tokens.length > 2) {
            if (tokens[3].startsWith("(")) {
                methodName = tokens[2];
                signature = tokens[3];
            } else {
                pathname = tokens[2];
                lineNumber = Integer.decode(tokens[3]);
            }
        }

        methodMap.put(methodId, new MethodSpec(className, methodName, signature, pathname, lineNumber));
    }

    private String constructPathname(String className, String pathname) {
        int index = className.lastIndexOf('/');
        if (index > 0 && index < className.length() - 1
                && pathname.endsWith(".java")) {
            pathname = className.substring(0, index + 1) + pathname;
        }
        return pathname;
    }

    private void parseThread(String line, List<ThreadInfo> threadInfoList, Map<Integer, TraceThreadInfo> threadMap) {
        int index = line.indexOf('\t');
        if (index != -1) {
            int threadId = Integer.parseInt(line.substring(0, index));
            String threadName = line.substring(index + 1);
            TraceThreadInfo threadInfo = new TraceThreadInfo(threadId, threadName);
            threadInfoList.add(threadInfo);
            threadMap.put(threadId, threadInfo);
        }
    }

    private enum ClockSource {
        THREAD_CPU, WALL, DUAL,
    }

    private ClockSource parseOption(String line, Map<String, String> propertiesMap) {
        int index = line.indexOf('=');
        if (index != -1) {
            String key = line.substring(0, index);
            String value = line.substring(index + 1);
            propertiesMap.put(key, value);

            if (key.equals("clock")) {
                switch (value) {
                    case "thread-cpu":
                        return ClockSource.THREAD_CPU;
                    case "wall":
                        return ClockSource.WALL;
                    case "dual":
                        return ClockSource.DUAL;
                }
            }
        }
        return null;
    }

    public List<ThreadInfo> getThreads() {
        return threadInfos;
    }

    @Override
    public boolean dumpStackTrace(PrintStream out, String keywords) {
        boolean dumped = false;
        for (ThreadInfo threadInfo : threadInfos) {
            dumped |= processCallNode(threadInfo, threadInfo.getChildren(), out, keywords, true);
        }
        if (!dumped) {
            for (ThreadInfo threadInfo : threadInfos) {
                dumped |= processCallNode(threadInfo, threadInfo.getChildren(), out, keywords, false);
            }
        }
        return dumped;
    }

    private boolean processCallNode(CallNode node, MethodCallNode[] children, PrintStream out, String keywords, boolean exact) {
        if (children.length < 1) { // leaf
            if (node.matchesStackElement(keywords, exact)) {
                out.println(node.getStackTraceString());
                return true;
            }
            return false;
        }

        boolean flag = false;
        for (MethodCallNode child : children) {
            flag |= processCallNode(child, child.getChildren(), out, keywords, exact);
        }
        return flag;
    }
}
