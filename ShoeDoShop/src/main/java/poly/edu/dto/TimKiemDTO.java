package poly.edu.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TimKiemDTO {
    private String        keyword;
    private LocalDateTime thoiGian;
}