package kz.kcell.assetpatchservice;

import kz.kcell.assetpatchservice.rollout_sites.RolloutSitesGetVars;
import kz.kcell.assetpatchservice.vpnportprocess.VpnPortProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class AssetPatchServiceApplication implements ApplicationRunner {

    @Autowired
    VpnPortProcessService vpnPortProcessService;
    @Autowired
    RolloutSitesGetVars rolloutSitesGetVars;

    public static void main(String[] args) {
        SpringApplication.run(AssetPatchServiceApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        vpnPortProcessService.insertIntoDb();
        rolloutSitesGetVars.reader();
    }
}
