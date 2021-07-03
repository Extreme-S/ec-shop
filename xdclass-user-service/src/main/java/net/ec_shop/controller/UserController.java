package net.ec_shop.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.ec_shop.enums.BizCodeEnum;
import net.ec_shop.request.UserLoginRequest;
import net.ec_shop.request.UserRegisterRequest;
import net.ec_shop.service.FileService;
import net.ec_shop.service.UserService;
import net.ec_shop.util.JsonData;
import net.ec_shop.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 不爱吃鱼的猫丶
 * @since 2021-06-26
 */
@Api(tags = "用户模块")
@RestController
@RequestMapping("/api/user/v1")
public class UserController {

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    /**
     * 上传用户头像
     * 默认最大是1M,超过则报错
     *
     * @param file 文件
     * @return
     */
    @ApiOperation("用户头像上传")
    @PostMapping(value = "upload")
    public JsonData uploadUserImg(
            @ApiParam(value = "文件上传", required = true)
            @RequestPart("file") MultipartFile file) {

        String result = fileService.uploadUserImg(file);
        return result != null ? JsonData.buildSuccess(result) : JsonData.buildResult(BizCodeEnum.FILE_UPLOAD_USER_IMG_FAIL);
    }

    /**
     * 用户注册
     *
     * @param registerRequest
     * @return
     */
    @ApiOperation("用户注册")
    @PostMapping("register")
    public JsonData register(@ApiParam("用户注册对象") @RequestBody UserRegisterRequest registerRequest) {
        JsonData jsonData = userService.register(registerRequest);
        return jsonData;
    }

    /**
     * 用户登录
     *
     * @return
     */
    @ApiOperation("用户登录")
    @PostMapping("login")
    public JsonData login(@ApiParam("用户登录对象") @RequestBody UserLoginRequest userLoginRequest) {

        JsonData jsonData = userService.login(userLoginRequest);

        return jsonData;
    }

    /**
     * 用户个人信息查询
     *
     * @return
     */
    @ApiOperation("个人信息查询")
    @GetMapping("detail")
    public JsonData detail() {

        UserVO userVO = userService.findUserDetail();

        return JsonData.buildSuccess(userVO);
    }


//    刷新token的方案
//    @PostMapping("refresh_token")
//    public JsonData getRefreshToken(Map<String,Object> param){
//
//        //先去redis,找refresh_token是否存在
//        //refresh_token存在，解密accessToken
//        //重新调用JWTUtil.geneJsonWebToken() 生成accessToken
//        //重新生成refresh_token，并存储redis，设置30天过期时间
//        //返回给前端
//        return null;
//    }


}

