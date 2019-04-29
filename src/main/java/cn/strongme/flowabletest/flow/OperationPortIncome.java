package cn.strongme.flowabletest.flow;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description:
 * email: <a href="strongwalter2014@gmail.com">阿水</a>
 *
 * @author 阿水
 * @date 2019-04-29 10:31.
 */

@Data
@NoArgsConstructor
public class OperationPortIncome implements OperationPort {

    private OperationPortType type = OperationPortType.Income;
    private String portId;

    public OperationPortIncome(String portId) {
        this.portId = portId;
    }

    @Override
    public String getConditionYes() {
        return null;
    }

    @Override
    public String getConditionNo() {
        return null;
    }
}
