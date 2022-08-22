package dz.mouradski.ftso.submitter.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PriceEpochData {

    private Long priceEpochId;
    private Long priceEpochStartTimestamp;
    private Long priceEpochEndTimestamp;
    private Long priceEpochRevealEndTimestamp;
    private Long currentTimestamp;
}
