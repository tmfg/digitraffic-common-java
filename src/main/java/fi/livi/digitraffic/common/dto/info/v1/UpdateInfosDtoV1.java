package fi.livi.digitraffic.common.dto.info.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.common.dto.data.v1.DataUpdatedSupportV1;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({ "dataUpdatedTime", "updateTimes" })
@Schema(description = "Info about API data updates (update intervals, last updated times)")
public class UpdateInfosDtoV1 implements DataUpdatedSupportV1 {

    @Schema(description = "Update times for APIs")
    public final List<UpdateInfoDtoV1> updateTimes;

    @Schema(description = "Latest update of data", requiredMode = Schema.RequiredMode.REQUIRED)
    private final Instant dataUpdatedTime;

    public UpdateInfosDtoV1(final List<UpdateInfoDtoV1> updateTimes, final Instant dataUpdatedTime) {
        this.updateTimes = updateTimes;
        this.dataUpdatedTime = dataUpdatedTime;
    }

    public Instant getDataUpdatedTime() {
        return dataUpdatedTime;
    }
}

