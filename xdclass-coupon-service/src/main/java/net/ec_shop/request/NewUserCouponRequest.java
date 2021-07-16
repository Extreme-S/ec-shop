package net.ec_shop.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel
@Data
public class NewUserCouponRequest {

    @ApiModelProperty(value = "用户Id", example = "19")
    @JsonProperty("user_id")
    private long userId;

    @ApiModelProperty(value = "用户昵称", example = "不爱吃鱼的猫丶")
    @JsonProperty("name")
    private String name;

}
