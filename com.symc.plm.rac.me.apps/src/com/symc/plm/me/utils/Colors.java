/**
 * 
 */
package com.symc.plm.me.utils;

import java.awt.Color;

/**
 * Class Name : Colors
 * Class Description : Color ฐüทร Util
 * @date 	2013. 11. 28.
 * @author anonymous
 * 
 */
public enum Colors {

    // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    // various colors in the pallete
    // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    Pink(255, 175, 175), Green(159, 205, 20), Orange(213, 113, 13), Yellow(Color.yellow), Red(189, 67, 67), LightBlue(
            208, 223, 245), Blue(Color.blue), Black(0, 0, 0), White(255, 255, 255), Gray(Color.gray
            .getRed(), Color.gray.getGreen(), Color.gray.getBlue()), LightGray(200, 200, 200);

    // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    // constructors
    // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

    Colors(Color c) {
        _myColor = c;
    }

    Colors(int r, int g, int b) {
        _myColor = new Color(r, g, b);
    }

    Colors(int r, int g, int b, int alpha) {
        _myColor = new Color(r, g, b, alpha);
    }

    Colors(float r, float g, float b, float alpha) {
        _myColor = new Color(r, g, b, alpha);
    }

    // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    // data
    // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

    private Color _myColor;

    // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    // methods
    // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

    public Color alpha(float t) {
        return new Color(_myColor.getRed(), _myColor.getGreen(), _myColor.getBlue(), (int) (t * 255f));
    }

    public static Color alpha(Color c, float t) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (t * 255f));
    }

    public Color color() {
        return _myColor;
    }

    public Color color(float f) {
        return alpha(f);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("r=").append(_myColor.getRed()).append(", g=").append(_myColor.getGreen()).append(
                ", b=").append(_myColor.getBlue()).append("\n");
        return sb.toString();
    }

    public String toHexString() {
        Color color = _myColor;
        StringBuilder sb = new StringBuilder();
        sb.append("#");
        sb.append(Integer.toHexString(color.getRed()));
        sb.append(Integer.toHexString(color.getGreen()));
        sb.append(Integer.toHexString(color.getBlue()));
        return sb.toString();
    }

}// end enum Colors
