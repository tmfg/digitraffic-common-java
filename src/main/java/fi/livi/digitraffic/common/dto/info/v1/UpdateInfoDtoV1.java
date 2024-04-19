package fi.livi.digitraffic.common.dto.info.v1;

import static fi.livi.digitraffic.common.util.TimeUtil.withoutMillis;

import java.time.Instant;

import fi.livi.digitraffic.common.dto.data.v1.DataUpdatedSupportV1;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Info about API data updates (update intervals, last updated times)", requiredMode = Schema.RequiredMode.REQUIRED)
public class UpdateInfoDtoV1 implements DataUpdatedSupportV1 {

    @NotNull
    @Schema(description = "Url of the API")
    final public String api;

    @Schema(description = "More specific info about API. Ie. domain info.")
    final public String subtype;

    @NotNull
    @Schema(description = "Latest update of data")
    private final Instant dataUpdatedTime;

    @Schema(description = "Latest check for data updates.  <br>\n" +
            "`null` value indicates data being pushed to our platform or static data that is only updated when needed.")
    public final Instant dataCheckedTime;

    @Schema(description = "Data update interval in ISO-8601 duration format `PnDTnHnMn.nS`. <br>\n" +
            "`P0S` means that data is updated in nearly real time. <br>\n" +
            "`null` means that data is static and updated only when needed.",
            type = "string",
            example = "[PT5M, P1H]")
    public final String dataUpdateInterval;

    @NotNull
    @Schema(description = "Recommended fetch interval for clients in ISO-8601 duration format `PnDTnHnMn.nS`",
            type = "string",
            example = "[PT5M, P1H]")
    public final String recommendedFetchInterval;

    public UpdateInfoDtoV1(final String api,
                           final String subtype,
                           final Instant dataUpdatedTime,
                           final Instant dataCheckedTime,
                           final String dataUpdateInterval,
                           final String recommendedFetchInterval) {
        this.api = api;
        this.subtype = subtype;
        this.dataUpdatedTime = withoutMillis(dataUpdatedTime);
        this.dataCheckedTime = withoutMillis(dataCheckedTime);
        this.dataUpdateInterval = dataUpdateInterval;
        this.recommendedFetchInterval = recommendedFetchInterval;
    }

    public UpdateInfoDtoV1(final String api,
                           final Instant dataUpdatedTime,
                           final Instant dataCheckedTime,
                           final String dataUpdateInterval,
                           final String recommendedFetchInterval) {
        this(api, null, dataUpdatedTime, dataCheckedTime, dataUpdateInterval, recommendedFetchInterval);
    }

    public UpdateInfoDtoV1(final String api,
                           final Instant dataUpdatedTime,
                           final String dataUpdateInterval,
                           final String recommendedFetchInterval) {
        this(api, dataUpdatedTime, null, dataUpdateInterval, recommendedFetchInterval);
    }

    public static UpdateInfoDtoV1 staticData(final String api, final Instant updatedTime) {
        return new UpdateInfoDtoV1(api, updatedTime, null, null, "P1D");
    }

    @Override
    public Instant getDataUpdatedTime() {
        return dataUpdatedTime;
    }
}

