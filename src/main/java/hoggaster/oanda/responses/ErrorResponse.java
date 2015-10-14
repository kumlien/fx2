package hoggaster.oanda.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {

    public final int code;

    public final String message;

    public final String moreInfo;

    @JsonCreator
    public ErrorResponse(@JsonProperty("code") int code, @JsonProperty("message") String message, @JsonProperty("moreInfo") String moreInfo) {
        this.code = code;
        this.message = message;
        this.moreInfo = moreInfo;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ErrorResponse [code=").append(code).append(", message=").append(message).append(", moreInfo=").append(moreInfo).append("]");
        return builder.toString();
    }
}
