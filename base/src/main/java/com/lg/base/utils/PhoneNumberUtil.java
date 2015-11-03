package com.lg.base.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by 007 on 2015/4/17.
 */
public class PhoneNumberUtil {

    public static enum Phone_Type{
        yidong,dianxin,liantong,unknow,invalid
    }
    public static Phone_Type isValidPhoneNumber(String phoneNumber){
        if(phoneNumber == null || phoneNumber.trim().length() != 11){
            return Phone_Type.invalid;
        }
        if(!phoneNumber.matches("[0-9]{11}")){
            return Phone_Type.invalid;
        }
        List<String> yidongList = Arrays.asList("134,135,136,137,138,139,147,150,151,152,157,158,159,182,183,187,188".split(","));
        List<String> liangList = Arrays.asList("130,131,132,155,156,185,186,145".split(","));
        List<String> dianxinList = Arrays.asList("133,153,177,180,181,189".split(","));

        String num = phoneNumber.substring(0,3);
        //说明是虚拟运营商
        if("170".equals(num)){
            String fourChar = phoneNumber.substring(0,4);
            if("1705".equals(fourChar)){
                return Phone_Type.yidong;
            }
            if("1709".equals(fourChar)){
                return Phone_Type.liantong;
            }
            if("1700".equals(fourChar)){
                return Phone_Type.dianxin;
            }
            return Phone_Type.unknow;
        }
        if(yidongList.contains(num)){
            return Phone_Type.yidong;
        }
        if(liangList.contains(num)){
            return Phone_Type.liantong;
        }
        if(dianxinList.contains(num)){
            return Phone_Type.dianxin;
        }
        return Phone_Type.unknow;
    }

    public static String getAnonymousPhoneNum(String phoneNum){
        if(phoneNum == null)
            return "unknow";
        if(isValidPhoneNumber(phoneNum)==Phone_Type.unknow){
            return "unknow";
        }
        String pre = phoneNum.substring(0,3);
        String sufx = phoneNum.substring(phoneNum.length()-4);
        return String.format(pre+"%s"+sufx,"XXXX");

    }


}
