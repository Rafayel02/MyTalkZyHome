package com.app.talkzy;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class ContinentData {

    private static String[][] continentData = {
            {"Africa","Algeria","Angola","Benin","Botswana","Burkina Faso","Burundi","Cabo Verde","Cameroon","Central African Republic (CAR)","Chad","Comoros","Congo, Democratic Republic of the","Congo, Republic of the","Cote d'Ivoire","Djibouti","Egypt","Equatorial Guinea","Eritrea","Eswatini (formerly Swaziland)","Ethiopia","Gabon","Gambia","Ghana","Guinea","Guinea-Bissau","Kenya","Lesotho","Liberia","Libya","Madagascar","Malawi","Mali","Mauritania","Mauritius","Morocco","Mozambique","Namibia","Niger","Nigeria","Rwanda","Sao Tome and Principe","Senegal","Seychelles","Sierra Leone","Somalia","South Africa","South Sudan","Sudan","Tanzania","Tunisia","Uganda","Zambia","Zimbabwe"},
            {"Asia","Kazakhstan","Kyrgyzstan","Tajikistan","Turkmenistan","Uzbekistan","Armenia","Azerbaijan","Bahrain","Cyprus","Georgia","Iraq","Israel","Jordan","Kuwait","Lebanon","Oman","Qatar","Saudi Arabia","State of Palestine","Syrian Arab Republic","Turkey","United Arab Emirates","Yemen","Afghanistan","Bangladesh","Bhutan","India","Iran (Islamic Republic of)","Maldives","Nepal","Pakistan","Sri Lanka","Brunei Darussalam","Cambodia","Indonesia","Lao People's Democratic Republic","Malaysia","Myanmar","Philippines","Singapore","Thailand","Timor-Leste","Viet Nam","China ","Japan","Mongolia","Republic of Korea","Hong Kong"},
            {"Australia", "Australia","Fiji","Kiribati","Marshall Islands","Micronesia","Nauru","New Zealand","Palau","Papua New Guinea","Samoa","Solomon Islands","Tonga","Tuvalu","Vanuatu"},
            {"Europe","Albania","Andorra","Armenia","Austria","Azerbaijan","Belarus","Belgium","Bosnia and Herzegovina","Bulgaria","Croatia","Cyprus","CzechRepublic","Denmark","Estonia","Finland","France","Georgia","Germany","Greece","Hungary","Iceland","Ireland","Italy","Kosovo","Latvia","Liechtenstein","Lithuania","Luxembourg","Macedonia","Malta","Moldova","Monaco","Montenegro","TheNetherlands","Norway","Poland","Portugal","Romania","Russia","SanMarino","Serbia","Slovakia","Slovenia","Spain","Sweden","Switzerland","Turkey","Ukraine","UnitedKingdom","VaticanCity"},
            {"North America","Antigua and Barbuda","Bahamas","Barbados","Belize","Canada","Costa Rica","Cuba","Dominica","Dominican Republic","El Salvador","Grenada","Guatemala","Haiti","Honduras","Jamaica","Mexico","Nicaragua","Panama","Saint Kitts and Nevis","Saint Lucia","Saint Vincent and the Grenadines","Trinidad and Tobago","United States of America","USA"},
            {"South America","Argentina","Bolivia","Brazil","Chile","Colombia","Ecuador","Guyana","Paraguay","Peru","Suriname","Uruguay","Venezuela"}
    };

    @Nullable
    @Contract(pure = true)
    public static String getContinent(String country) {
        if(country == null) {
            return continentData[3][0];
        }
        int position = 3;
        OUT:for(int i=0; i<continentData.length; i++) {
            for(int j=0; j<continentData[i].length; j++){
                if(continentData[i][j].equals(country)) {
                    position = i;
                    break OUT;
                }
            }
        }
        return continentData[position][0];
    }
}
