package ccc.keeweapi.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkCreateRequestDto {
    private Long profileId;
    private String link;
}
