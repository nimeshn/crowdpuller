package com.bitwinger.crowdpuller.masters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nimesh on 06-02-2016.
 */
public class Categories {
    private static List<Category> rows = new ArrayList<Category>();
    private static Map<Integer, String> flatMap;
    private static CharSequence[] flatRows;

    public static void fillData() {
        if (rows.size() > 0)
            return;
        Category cat = new Category(1, "community");
        cat.addChild(7, "activities");
        cat.addChild(8, "childcare");
        cat.addChild(9, "classes");
        cat.addChild(10, "events");
        cat.addChild(11, "general");
        cat.addChild(12, "groups");
        cat.addChild(13, "local news");
        cat.addChild(14, "lost/found");
        cat.addChild(15, "rideshare");
        cat.addChild(16, "volunteers");
        cat.addChild(17, "Others");
        rows.add(cat);

        cat = new Category(2, "Business Promotions");
        cat.addChild(18, "Clothing");
        cat.addChild(19, "Sports");
        cat.addChild(20, "Food/Dining");
        cat.addChild(21, "SuperMarkets");
        cat.addChild(22, "Malls");
        cat.addChild(23, "Jewelery");
        cat.addChild(24, "General store");
        cat.addChild(25, "Grocery store");
        cat.addChild(26, "Hardware Store");
        cat.addChild(27, "Pet Store");
        cat.addChild(28, "Picture");
        cat.addChild(29, "Shoe Store");
        cat.addChild(30, "Toy Store");
        cat.addChild(31, "Electronics");
        cat.addChild(32, "Others");
        rows.add(cat);

        cat = new Category(3, "services");
        cat.addChild(33, "automotive");
        cat.addChild(34, "beauty");
        cat.addChild(35, "computer");
        cat.addChild(36, "creative");
        cat.addChild(37, "cycle");
        cat.addChild(38, "event");
        cat.addChild(39, "farm+garden");
        cat.addChild(40, "financial");
        cat.addChild(41, "household");
        cat.addChild(42, "labor/movers");
        cat.addChild(43, "legal");
        cat.addChild(44, "lessons");
        cat.addChild(45, "Pets");
        cat.addChild(46, "Classes");
        cat.addChild(47, "real estate");
        cat.addChild(48, "Healthcare");
        cat.addChild(49, "travel/vacation");
        cat.addChild(50, "Others");
        rows.add(cat);

        cat = new Category(4, "housing");
        cat.addChild(51, "apts / housing");
        cat.addChild(52, "housing wanted");
        cat.addChild(53, "office / commercial");
        cat.addChild(54, "parking / storage");
        cat.addChild(55, "real estate for sale");
        cat.addChild(56, "rooms / shared");
        cat.addChild(57, "rooms wanted");
        cat.addChild(58, "sublets / temporary");
        cat.addChild(59, "vacation rentals");
        cat.addChild(60, "PG");
        cat.addChild(61, "Others");
        rows.add(cat);

        cat = new Category(5, "for sale");
        cat.addChild(62, "antiques");
        cat.addChild(63, "appliances");
        cat.addChild(64, "arts+crafts");
        cat.addChild(65, "atv/utv/sno");
        cat.addChild(66, "auto parts");
        cat.addChild(67, "bikes");
        cat.addChild(68, "books");
        cat.addChild(69, "business");
        cat.addChild(70, "cars+trucks");
        cat.addChild(71, "cds/dvd/vhs");
        cat.addChild(72, "cell phones");
        cat.addChild(73, "clothes+acc");
        cat.addChild(74, "collectibles");
        cat.addChild(75, "computers");
        cat.addChild(76, "electronics");
        cat.addChild(77, "farm+garden");
        cat.addChild(78, "free");
        cat.addChild(79, "furniture");
        cat.addChild(80, "garage sale");
        cat.addChild(81, "general");
        cat.addChild(82, "heavy equip");
        cat.addChild(83, "household");
        cat.addChild(84, "jewelry");
        cat.addChild(85, "materials");
        cat.addChild(86, "motorcycles");
        cat.addChild(87, "music instr");
        cat.addChild(88, "photo+video");
        cat.addChild(89, "sporting");
        cat.addChild(90, "tickets");
        cat.addChild(91, "tools");
        cat.addChild(92, "toys+games");
        cat.addChild(93, "video gaming");
        cat.addChild(94, "wanted");
        cat.addChild(95, "Others");
        rows.add(cat);

        cat = new Category(6, "jobs");
        cat.addChild(96, "accounting+finance");
        cat.addChild(97, "admin / office");
        cat.addChild(98, "arch / engineering");
        cat.addChild(99, "art / media / design");
        cat.addChild(100, "biotech / science");
        cat.addChild(101, "business / mgmt");
        cat.addChild(102, "customer service");
        cat.addChild(103, "education");
        cat.addChild(104, "food / bev / hosp");
        cat.addChild(105, "general labor");
        cat.addChild(106, "government");
        cat.addChild(107, "human resources");
        cat.addChild(108, "internet engineers");
        cat.addChild(109, "legal / paralegal");
        cat.addChild(110, "manufacturing");
        cat.addChild(111, "marketing / pr / ad");
        cat.addChild(112, "medical / health");
        cat.addChild(113, "nonprofit sector");
        cat.addChild(114, "real estate");
        cat.addChild(115, "retail / wholesale");
        cat.addChild(116, "sales / biz dev");
        cat.addChild(117, "salon / spa / fitness");
        cat.addChild(118, "security");
        cat.addChild(119, "skilled trade / craft");
        cat.addChild(120, "software / qa / dba");
        cat.addChild(121, "systems / network");
        cat.addChild(122, "technical support");
        cat.addChild(123, "transport");
        cat.addChild(124, "tv / film / video");
        cat.addChild(125, "web / info design");
        cat.addChild(126, "writing / editing");
        cat.addChild(127, "Others");
        rows.add(cat);
        //
        flatMap = new LinkedHashMap<Integer, String>();
        for (int i = 0; i < rows.size(); i++) {
            for (Category temp : rows.get(i).getChildrens()) {
                flatMap.put(temp.getId(), rows.get(i).getCode() + " - " + temp.getCode());
            }
        }
        flatRows = flatMap.values().toArray(new CharSequence[flatMap.size()]);
    }

    public static List<Category> getRows() {
        return rows;
    }

    public static CharSequence[] getArrayList() {
        return flatRows;
    }

    public static Integer getCatIdFromArrayIndex(Integer index) {
        int i = 0;
        for (Map.Entry<Integer, String> entry : flatMap.entrySet()) {
            if (index == i) {
                return entry.getKey();
            }
            i++;
        }
        return -1;
    }

    public static String getCatCodeFromCatId(Integer catId) {
        return flatMap.get(catId);
    }
}