package com.junctionx.backend.session.service;
import com.junctionx.backend.model.Job;
import com.junctionx.backend.model.enums.ProductType;
import com.junctionx.backend.model.enums.DecisionType;
public class OfferProcessor {


// Uncomment these when you implement the logic:
// import <your.package>.domain.Score;
// import <your.package>.domain.EvaluationModel;
// import <your.package>.domain.ThresholdEvaluator;
public class OfferProcessor {


        public DecisionType processOffer(OfferInput in) {
            Job jobOffer = new Job(
                    in.cityId(),
                    in.productType(),
                    in.pickupLat(),
                    in.pickupLon(),
                    in.pickupHexId9(),
                    in.dropLat(),
                    in.dropLon(),
                    in.dropHexId9(),
                    in.distanceKm(),
                    in.netEarnings(),
                    in.durationMins()
            );
            return null; ///evaluateAndDecide(jobOffer);
        }


        /** Minimal DTO mirroring your Job constructor arguments. */
        public record OfferInput(
                Integer cityId,
                ProductType productType,
                Double pickupLat,
                Double pickupLon,
                String pickupHexId9,
                Double dropLat,
                Double dropLon,
                String dropHexId9,
                Double distanceKm,
                Double netEarnings,
                Integer durationMins
        ) {}
    }

}
