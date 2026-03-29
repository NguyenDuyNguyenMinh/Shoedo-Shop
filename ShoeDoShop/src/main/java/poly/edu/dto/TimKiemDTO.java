package poly.edu.dto;

import lombok.*;
import java.util.Date;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TimKiemDTO {
    private String keyword;
    private Date   thoiGian;
}
