package kz.kcell.assetpatchservice.vpnportprocess;

import kz.kcell.assetpatchservice.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class VpnPortProcessService {
    private final SimpleJdbcInsert insertIntoAddresses;
    private final SimpleJdbcInsert insertIntoPort;
    private final SimpleJdbcInsert insertIntoVpn;

    public VpnPortProcessService(JdbcTemplate jdbcTemplate) {
        insertIntoAddresses = new SimpleJdbcInsert(jdbcTemplate).withTableName("adresses").usingGeneratedKeyColumns("id");
        insertIntoPort = new SimpleJdbcInsert(jdbcTemplate).withTableName("port").usingGeneratedKeyColumns("id");
        insertIntoVpn = new SimpleJdbcInsert(jdbcTemplate).withTableName("vpn").usingGeneratedKeyColumns("id");
    }

    public void insertIntoDb() throws Exception {
        Map<Long, Map<String, String>> addresses = Util.readCsvIntoMap("vpn-port-process/KWMS-1742/addresses.csv");
        Map<Long, Map<String, String>> ports = Util.readCsvIntoMap("vpn-port-process/KWMS-1742/port.csv");
        Map<Long, Map<String, String>> vpns = Util.readCsvIntoMap("vpn-port-process/KWMS-1742/vpn.csv");

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

        log.info("------------------- INSERT ADDRESSES -------------------");
        for (Map.Entry<Long, Map<String, String>> address : addresses.entrySet()) {
            long id = insertIntoAddresses(
                    format.parse(address.getValue().get("date_created")),
                    format.parse(address.getValue().get("date_updated")),
                    Long.valueOf(address.getValue().get("city_id")),
                    Long.valueOf(address.getValue().get("city_catalog_id")),
                    Boolean.parseBoolean(address.getValue().get("not_full_address")),
                    address.getValue().get("status")
            );
            log.info("Inserted address #={} with id={}", address.getKey(), id);

            address.getValue().put("id", String.valueOf(id));
        }

        log.info("------------------- INSERT PORT -------------------");
        for (Map.Entry<Long, Map<String, String>> port : ports.entrySet()) {
            Map<String, String> address = addresses.get(Long.parseLong(port.getValue().get("port_termination_point_id")));
            if (address == null) continue;

            long id = insertIntoPort(
                    format.parse(port.getValue().get("date_created")),
                    format.parse(port.getValue().get("date_updated")),
                    port.getValue().get("channel_type"),
                    Integer.parseInt(port.getValue().get("port_capacity")),
                    port.getValue().get("port_capacity_unit"),
                    port.getValue().get("port_number"),
                    port.getValue().get("port_type"),
                    port.getValue().get("status"),
                    Long.parseLong(address.get("id"))
            );
            log.info("Inserted port #={} with id={}", port.getKey(), id);

            port.getValue().put("id", String.valueOf(id));
        }

        log.info("------------------- INSERT VPN -------------------");
        for (Map.Entry<Long, Map<String, String>> vpn : vpns.entrySet()) {
            Map<String, String> address = addresses.get(Long.parseLong(vpn.getValue().get("vpn_termination_point_2")));
            if (address == null) continue;

            Map<String,String> port = ports.get(Long.parseLong(vpn.getValue().get("port_id")));

            long id = insertIntoVpn(
                    format.parse(vpn.getValue().get("date_created")),
                    format.parse(vpn.getValue().get("date_updated")),
                    vpn.getValue().get("kcell_as"),
                    vpn.getValue().get("kcell_ip"),
                    vpn.getValue().get("provider_as"),
                    vpn.getValue().get("provider_ip"),
                    vpn.getValue().get("service"),
                    !StringUtils.isBlank(vpn.getValue().get("service_capacity")) ? Integer.parseInt(vpn.getValue().get("service_capacity").split(",")[0]) : null,
                    Long.parseLong(vpn.getValue().get("service_type_catalog_id")),
                    !StringUtils.isBlank(vpn.getValue().get("service_type_id")) ? Long.parseLong(vpn.getValue().get("service_type_id")) : null,
                    vpn.getValue().get("status"),
                    vpn.getValue().get("vlan"),
                    vpn.getValue().get("vpn_number"),
                    Long.parseLong(address.get("id")),
                    Long.parseLong(port.get("id"))
            );
            log.info("Inserted vpn #={} with id={}", vpn.getKey(), id);
        }

        log.info("------------------- INSERTION DONE -------------------");
    }

    public Long insertIntoAddresses(Date dateCreated, Date dateUpdated, Long cityId, Long cityCatalogId, boolean notFullAddress, String status) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("date_created", dateCreated);
        parameters.put("date_updated", dateUpdated);
        parameters.put("city_id", cityId);
        parameters.put("city_catalog_id", cityCatalogId);
        parameters.put("not_full_address", notFullAddress);
        parameters.put("status", status);

        Number generatedId = insertIntoAddresses.executeAndReturnKey(parameters);
        return generatedId.longValue();
    }

    public Long insertIntoPort(Date dateCreated, Date dateUpdated, String channelType, int portCapacity, String portCapacityUnit, String portNumber, String portType, String status, Long portTerminationPoint) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("date_created", dateCreated);
        parameters.put("date_updated", dateUpdated);
        parameters.put("channel_type", channelType);
        parameters.put("port_capacity", portCapacity);
        parameters.put("port_capacity_unit", portCapacityUnit);
        parameters.put("port_number", portNumber);
        parameters.put("port_type", portType);
        parameters.put("status", status);
        parameters.put("port_termination_point_id", portTerminationPoint);

        Number generatedId = insertIntoPort.executeAndReturnKey(parameters);
        return generatedId.longValue();
    }

    public Long insertIntoVpn(Date dateCreated, Date dateUpdated, String kcellAs, String kcellIp, String providerAs, String providerIp,
                              String service, Integer serviceCapacity, Long serviceTypeCatalogId, Long serviceTypeId, String status,
                              String vlan, String vpnNumber, Long vpnTerminationPoint2Id, Long portId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("date_created", dateCreated);
        parameters.put("date_updated", dateUpdated);
        parameters.put("kcell_as", kcellAs);
        parameters.put("kcell_ip", kcellIp);
        parameters.put("provider_as", providerAs);
        parameters.put("provider_ip", providerIp);
        parameters.put("service", service);
        parameters.put("service_capacity", serviceCapacity);
        parameters.put("service_type_catalog_id", serviceTypeCatalogId);
        parameters.put("service_type_id", serviceTypeId);
        parameters.put("status", status);
        parameters.put("vlan", vlan);
        parameters.put("vpn_number", vpnNumber);
        parameters.put("vpn_termination_point_2_id", vpnTerminationPoint2Id);
        parameters.put("port_id", portId);

        Number generatedId = insertIntoVpn.executeAndReturnKey(parameters);
        return generatedId.longValue();
    }

}
