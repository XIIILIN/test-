package automail;

import com.unimelb.swen30006.wifimodem.WifiModem;

import java.util.HashMap;
import java.util.Map;

public class ServiceFeeCalculatorImpl implements ServiceFeeCalculator {
    Map<Integer, Double> serviceFeeMap;
    Building building;

    public ServiceFeeCalculatorImpl(Building building) {
        this.building = building;
        serviceFeeMap = new HashMap<>();
    }

    public double calculateServiceFee(int targetFloor) {
        try {
            WifiModem r = WifiModem.getInstance(building.getMailroomLocationFloor());
            double fee = r.forwardCallToAPI_LookupPrice(targetFloor);
            if (fee < 0) throw new Exception();
            serviceFeeMap.put(targetFloor, fee);
            return fee;
        } catch (Exception e) {
        }

        if (serviceFeeMap.containsKey(targetFloor)) {
            return serviceFeeMap.get(targetFloor);
        }
        return 0;
    }
}
