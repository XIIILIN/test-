package automail;

import java.util.HashMap;
import java.util.Map;

public class ServiceTimeCalculatorImpl implements ServiceTimeCalculator {

    Map<RobotType, Integer> workMap;

    public ServiceTimeCalculatorImpl() {
        workMap = new HashMap<>();
    }

    @Override
    public void doService(int time, RobotType type) {
        if (workMap.containsKey(type)) {
            workMap.put(type, workMap.get(type) + 1);
        } else {
            workMap.put(type, 1);
        }
    }

    @Override
    public int getServiceTime(RobotType type) {
        if (workMap.containsKey(type)) {
            return workMap.get(type);
        }
        return 0;
    }
}
