package com.bitwinger.crowdpuller.masters;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Nimesh on 06-02-2016.
 */
public class FlagReasons {
    private static Map<Integer, String> list = new LinkedHashMap<Integer, String>();

    public static Map<Integer, String> getList() {
        return list;
    }

    public static void populate() {
        if (list.size() == 0) {
            list.put(1, "false, misleading, deceptive, or fraudulent content");
            list.put(2, "offensive, obscene, defamatory, threatening, or malicious content");
            list.put(3, "anyones personal, identifying, confidential or proprietary information");
            list.put(4, "child pornography; bestiality; offers or solicitation of illegal prostitution");
            list.put(5, "spam; miscategorized, overposted, cross-posted, or nonlocal content");
            list.put(6, "Selling stolen property, property with serial number removed/altered, burglary tools, etc");
            list.put(7, "Selling ID cards, licenses, police insignia, government documents, birth certificates, etc");
            list.put(8, "Selling counterfeit, replica, or pirated items;");
            list.put(9, "Selling lottery or raffle tickets, gambling items");
            list.put(10, "affiliate marketing; network, or multi-level marketing; pyramid schemes");
            list.put(11, "Selling ivory; endangered, imperiled and/or protected species and any parts thereof");
            list.put(12, "Selling alcohol or tobacco;");
            list.put(13, "Selling prescription drugs, controlled substances and related items");
            list.put(14, "Selling weapons; firearms/guns; etc");
            list.put(15, "Selling ammunition, gunpowder, explosives");
            list.put(16, "Selling hazardous materials; body parts/fluids;");
            list.put(17, "any good, service, or content that violates the law or legal rights of others");
        }
    }

    public static CharSequence[] getArrayList(){
        return list.values().toArray(new CharSequence[list.size()]);
    }
}