import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Query {

    private final String QUERY_REGEX = "^[QTRE]:\\(\\d+\\);\\([^)]+\\)(;\\([^)]+\\))*$";
    private static AtomicInteger count = new AtomicInteger(0); // this is for auto id

    public final QueryType type;
    public final Integer id;
    public final List<String> msgList;

    /**
     * Construct a query object with given type and messages
     *
     * @param type    - type of this query
     * @param msgList - list of messages to send
     * @throws QueryFormatException when there is no message
     */
    public Query(QueryType type, List<String> msgList) throws QueryFormatException {
        this.type = type;
        this.id = count.incrementAndGet();
        if (msgList.size() == 0) {
            throw new QueryFormatException();
        }
        this.msgList = msgList;
    }

    /**
     * Returns a String representation of the query
     */
    @Override
    public String toString() {
        String queryMessage = msgList.stream().map(msg -> "(" + msg + ")").reduce((msg1, msg2) -> msg1 + ";" + msg2)
                .orElse("");
        return String.format("%s:(%d);%s", type.toString(), id, queryMessage);
    }

    /**
     * Construct a query object from a given query string
     *
     * @param queryString - a properly formatted query string
     * @throws Exception when the query string is not properly formatted or the id
     *                   is already taken
     */
    public Query(String queryString) throws QueryFormatException {
        if (!queryString.matches(QUERY_REGEX)) {
            throw new QueryFormatException();
        }
        this.type = QueryType.valueOf(queryString.substring(0, 1));
        List<String> entryList = Arrays.stream(queryString.substring(2).split(";"))
                .map(msg -> msg.replaceAll("[()]", "")).collect(Collectors.toList());
        Integer potentialId = Integer.parseInt(entryList.get(0));
        if (potentialId < count.get()) {
            throw new NumberFormatException("Invalid id: " + potentialId + ", must be larger than: " + count.get());
        }
        this.id = potentialId;
        count.set(this.id);
        this.msgList = entryList.subList(1, entryList.size());
    }

}
