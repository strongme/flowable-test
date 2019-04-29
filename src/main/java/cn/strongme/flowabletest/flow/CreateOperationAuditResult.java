package cn.strongme.flowabletest.flow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description:
 * email: <a href="strongwalter2014@gmail.com">阿水</a>
 *
 * @author 阿水
 * @date 2019-04-26 09:48.
 */

@NoArgsConstructor
@Data
@AllArgsConstructor
public class CreateOperationAuditResult {

    private OperationPort income;

    private OperationPort output;


}
