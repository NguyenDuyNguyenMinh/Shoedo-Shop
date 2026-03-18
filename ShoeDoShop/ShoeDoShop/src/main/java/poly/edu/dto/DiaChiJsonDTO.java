package poly.edu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class DiaChiJsonDTO {

    @JsonProperty("TenNN")
    private String tenNN;

    @JsonProperty("SDT")
    private String sdt;

    @JsonProperty("DiemGiao")
    private String diemGiao;
}