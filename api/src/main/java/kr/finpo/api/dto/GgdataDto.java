package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import kr.finpo.api.constant.OpenApiType;
import kr.finpo.api.domain.Policy;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GgdataDto(
    ArrayList<JobFndtnSportPolocyWrapper> JobFndtnSportPolocy
) {

    public record JobFndtnSportPolocyWrapper(
        ArrayList<Row> row
    ) {

        public record Row(
            String PBLANC_TITLE,
            String INST_NM,
            String RECRUT_BEGIN_DE,
            String RECRUT_END_DE,
            String DIV_CD,
            String DIV_NM,
            String REGION_CD,
            String REGION_NM,
            String DETAIL_PAGE_URL
        ) {

            public Policy toEntity() {
                LocalDate start = null, end = null;
                try {
                    start = LocalDate.parse(RECRUT_BEGIN_DE, DateTimeFormatter.ISO_DATE);
                } catch (Exception ignored) {
                }
                try {
                    end = LocalDate.parse(RECRUT_END_DE, DateTimeFormatter.ISO_DATE);
                } catch (Exception ignored) {
                }

                return Policy.of(PBLANC_TITLE.trim(), Integer.toString(PBLANC_TITLE.hashCode()), INST_NM, null, null,
                    null, null, null, start, end, null, null, DETAIL_PAGE_URL, OpenApiType.GGDATA);
            }
        }
    }
}
