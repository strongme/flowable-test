package cn.strongme.flowabletest.flow;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description:
 * email: <a href="strongwalter2014@gmail.com">阿水</a>
 *
 * @author 阿水
 * @date 2019-04-29 10:32.
 */

@Data
@NoArgsConstructor
public class OperationPortOutput implements OperationPort {

    private String portId;
    private OperationPortType type = OperationPortType.Output;

    public OperationPortOutput(String portId) {
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