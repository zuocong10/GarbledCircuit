package util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import gc.entity.Gate;

public class FastJsonTest {
	public static void main(String[] args) {
		
		Gate gate = (Gate)WRObject.readObjectFromFile("AND.bin");
		//String text = JSON.toJSONString(gate);
		String pretty = JSON.toJSONString(gate, SerializerFeature.PrettyFormat);
		
		System.out.println(pretty);
		WRFile.writeTxt("gate.json", pretty);
		
		
		Gate gate2 = JSON.parseObject(WRFile.readAll("gate.json"), Gate.class);
		System.out.println(gate2);
	}
}
