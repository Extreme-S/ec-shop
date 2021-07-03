package net.ec_shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.ec_shop.enums.BizCodeEnum;
import net.ec_shop.enums.SendCodeEnum;
import net.ec_shop.mapper.UserMapper;
import net.ec_shop.model.LoginUser;
import net.ec_shop.model.UserDO;
import net.ec_shop.request.UserLoginRequest;
import net.ec_shop.request.UserRegisterRequest;
import net.ec_shop.service.NotifyService;
import net.ec_shop.service.UserService;
import net.ec_shop.util.CommonUtil;
import net.ec_shop.util.JWTUtil;
import net.ec_shop.util.JsonData;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
@Slf4j
public class UserServiceImpl implements UserService {


    @Autowired
    private NotifyService notifyService;


    @Autowired
    private UserMapper userMapper;

    /**
     * 用户注册
     * * 邮箱验证码验证
     * * 密码加密（TODO）
     * * 账号唯一性检查(TODO)
     * * 插入数据库
     * * 新注册用户福利发放(TODO)
     *
     * @param registerRequest
     * @return
     */
    @Override
    public JsonData register(UserRegisterRequest registerRequest) {

        boolean checkCode = false;
        //校验验证码
        if (StringUtils.isNotBlank(registerRequest.getMail())) {
            checkCode = notifyService.checkCode(SendCodeEnum.USER_REGISTER, registerRequest.getMail(), registerRequest.getCode());
        }

        //
        if (!checkCode) {
            return JsonData.buildResult(BizCodeEnum.CODE_ERROR);
        }

        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(registerRequest, userDO);

        userDO.setCreateTime(new Date());
        userDO.setSlogan("人生需要动态规划，学习需要贪心算法");

        //设置密码 TODO
        //userDO.setPwd(registerRequest.getPwd());

        userDO.setSecret("$1$" + CommonUtil.getStringNumRandom(8));

        //密码+盐处理
        String cryptPwd = Md5Crypt.md5Crypt(registerRequest.getPwd().getBytes(), userDO.getSecret());
        userDO.setPwd(cryptPwd);

        //账号唯一性检查  TODO

        if (checkUnique(userDO.getMail())) {
            int rows = userMapper.insert(userDO);
            log.info("rows:{},注册成功:{}", rows, userDO.toString());

            //新用户注册成功，初始化信息，发放福利等 TODO
            userRegisterInitTask(userDO);
            return JsonData.buildSuccess();
        } else {
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_REPEAT);
        }

    }

    /**
     * 1、根据Mail去找有没这记录
     * 2、有的话，则用秘钥+用户传递的明文密码，进行加密，再和数据库的密文进行匹配
     *
     * @param userLoginRequest
     * @return
     */
    @Override
    public JsonData login(UserLoginRequest userLoginRequest) {

        List<UserDO> userDOList = userMapper.selectList(new QueryWrapper<UserDO>().eq("mail", userLoginRequest.getMail()));

        if (userDOList != null && userDOList.size() == 1) {
            //已经注册
            UserDO userDO = userDOList.get(0);
            String cryptPwd = Md5Crypt.md5Crypt(userLoginRequest.getPwd().getBytes(), userDO.getSecret());
            if (cryptPwd.equals(userDO.getPwd())) {
                //登录成功,生成token TODO
                LoginUser loginUser = new LoginUser();
                BeanUtils.copyProperties(userDO, loginUser);
                String token = JWTUtil.geneJsonWebToken(loginUser);
                // accessToken
                // accessToken的过期时间
                // UUID生成一个token
                //String refreshToken = CommonUtil.generateUUID();
                //redisTemplate.opsForValue().set(refreshToken,"1",1000*60*60*24*30);

                return JsonData.buildSuccess(token);
            } else {

                return JsonData.buildResult(BizCodeEnum.ACCOUNT_PWD_ERROR);
            }
        } else {
            //未注册
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNREGISTER);
        }


    }

    /**
     * 校验用户账号唯一 TODO
     *
     * @param mail
     * @return
     */
    private boolean checkUnique(String mail) {
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<UserDO>().eq("mail", mail);

        List<UserDO> list = userMapper.selectList(queryWrapper);

        return list.size() > 0 ? false : true;
    }


    /**
     * 用户注册，初始化福利信息 TODO
     *
     * @param userDO
     */
    private void userRegisterInitTask(UserDO userDO) {

    }


}
