

public class Standard {

    public static String standardiserIp(String line) throws NotStandardException{
        int i=0;
        String ip="";
        if(line.length() > 15 || line.length() < 7){
            throw new NotStandardException();
        }
        for(int k=line.length()-1; k>=0;k--){
            if(i == 0 && (line.charAt(k) < '0' || line.charAt(k) > '9')){
                throw new NotStandardException();
            }
            char actualChar;
            if(i == 0){
                actualChar = line.charAt(k);
                ip = actualChar + ip;
                i++;
            }else if(line.charAt(k) == '.'){
                for(int l=0 ; l< 3 - i; l++){
                    ip = "0" + ip;
                }
                ip = "." + ip;
                i=0;
                
            }else{
                if( i >= 3 || line.charAt(k) < '0' || line.charAt(k) > '9'){
                    throw new NotStandardException();
                }

                actualChar = line.charAt(k);
                ip = actualChar + ip;
                i++;
            }
        }
       return ip;
    }

    public static String standardiserNumMsg(int index) throws NotStandardException {
        if(index < 0 || index > 9999){
            throw new NotStandardException();
        }
        String index_str = Integer.toString(index);
        if(index < 10){
            index_str = "000" + index_str;
        }else if(index < 100){
            index_str = "00" + index_str;
        }else if(index < 1000){
            index_str = "0" + index_str;
        }
        return index_str;
    }

    public static String standardiserMsg(String line) {
        int len=line.length();
        if(len > 140){
            return line.substring(0, 140);
        }
        for (int j=0 ; j < 140 - len ; j++){
            line = line + "#";
        }
        return line;
    }
    public static String restituerMsg(String msg) throws NotStandardException{
        int len=msg.length();
        if(len != 140){
            throw new NotStandardException();
        }
        int i=len;
        while(msg.charAt(i-1) == '#' && i>1){
            i--;
        }
        return msg.substring(0, i);
    }

    public static String standardiserNumMsg3(int nb) throws NotStandardException{
        if(nb >=0 && nb<= 999){
            String nb_str= Integer.toString(nb);
            if(nb < 10 ){
                nb_str = "00" + nb_str;
            }else if(nb < 100){
                nb_str= "0"+ nb_str;
            }
            return nb_str;
        }else{
            throw new NotStandardException();
        }
    }
    public static void main(String[] args) {
        try{
        System.out.println(Standard.standardiserIp("127.0.0.1"));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    
}
