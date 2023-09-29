package kz.kcell.assetpatchservice.rollout_sites;

import lombok.Data;

@Data
public class DuplicateSites {
    private Long id;
    private String siteName;
    private String region_name;
    private String city_name;
    private String street;
    private String building;
    private String note;
    private String cadastral_number;
}
