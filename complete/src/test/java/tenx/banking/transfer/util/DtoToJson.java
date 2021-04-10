package tenx.banking.transfer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DtoToJson {
    public static String convert(Object dto) throws JsonProcessingException {
        String json = new ObjectMapper().writeValueAsString(dto);
        System.out.println(json);
        return json;
    }
}
