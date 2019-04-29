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
public class OperationPortOutputYesAndNo implements OperationPort {

    private OperationPortType type = OperationPortType.OutputYesAndNo;

    private String portId;

    private String conditionYes;

    private String conditionNo;

    public OperationPortOutputYesAndNo(String portId, String conditionYes, String conditionNo) {
        this.portId = portId;
        this.conditionYes = conditionYes;
        this.conditionNo = conditionNo;
    }
}
