package util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

import gc.entity.Circuit;
import gc.entity.GarbledGate;

public class FastJsonTest {
	public static void main(String[] args) {
		
		byte[] cir_b = WRFile.readAll("testcircuit_ranLabels.json");
		Circuit cir = JSON.parseObject(cir_b, Circuit.class);
		String pretty = JSON.toJSONString(cir, SerializerFeature.PrettyFormat);
		System.out.println(pretty.equals(new String(cir_b)));
		
		GarbledGate[] ggs = JSON.parseObject(new String(WRFile.readAll("garbledGates.json")), new TypeReference<GarbledGate[]>() {});
		System.out.println(ggs[0].gate_id);
	}
}
