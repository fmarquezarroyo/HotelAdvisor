package es.pfm.hoteladvisor.utils;

public class Util {

    public static boolean isNumeric(String str) {
        return str.matches("\\+?\\d+(\\.\\d+)?\\+?");
    }

    public static boolean isNumericBetween(String str, double min, double max) {

        if (str.startsWith("+"))
            str = str.substring(1);
        else if (str.endsWith("+"))
            str = str.substring(0,str.length()-1);

        return isNumeric(str) && Double.parseDouble(str) >= min && Double.parseDouble(str) <= max;
    }

    public static boolean areValidTypes(String str){
        String [] types = str.split(",");
        boolean valid = true;
        for (int i=0;i<types.length;i++){
            if(!isNumeric(types[i]))
                return false;
        }
        return valid;
    }

}
