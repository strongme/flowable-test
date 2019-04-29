package cn.strongme.flowabletest.flow;

/**
 * description:
 * email: <a href="strongwalter2014@gmail.com">阿水</a>
 *
 * @author 阿水
 * @date 2019-04-29 09:46.
 */

public interface OperationPort {

    String getPortId();

    OperationPortType getType();

    String getConditionYes();

    String getConditionNo();

}
