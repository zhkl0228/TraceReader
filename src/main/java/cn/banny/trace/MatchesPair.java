package cn.banny.trace;

class MatchesPair implements Comparable<MatchesPair> {

    private final MethodCallNode matches;
    final MethodCallNode leaf;

    MatchesPair(MethodCallNode matches, MethodCallNode leaf) {
        this.matches = matches;
        this.leaf = leaf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatchesPair pair = (MatchesPair) o;

        return matches != null ? matches.equals(pair.matches) : pair.matches == null;
    }

    @Override
    public int hashCode() {
        return matches != null ? matches.hashCode() : 0;
    }

    @Override
    public int compareTo(MatchesPair o) {
        if (matches.equals(o.matches)) {
            return o.leaf.getStackTraceSize() - leaf.getStackTraceSize();
        }

        return 0;
    }
}
