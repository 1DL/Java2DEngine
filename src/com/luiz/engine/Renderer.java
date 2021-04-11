/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.luiz.engine;

import com.luiz.engine.gfx.Font;
import com.luiz.engine.gfx.Image;
import com.luiz.engine.gfx.ImageRequest;
import com.luiz.engine.gfx.ImageTile;
import com.luiz.engine.gfx.Light;
import com.luiz.engine.gfx.LightRequest;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author Luiz
 */
public class Renderer
{

    private Font font = Font.STANDARD;
    private ArrayList<ImageRequest> imageRequest = new ArrayList<ImageRequest>();
    private ArrayList<LightRequest> lightRequest = new ArrayList<LightRequest>();

    //The aumont of pixels in the x and y axis (width and height) based on internal resolution
    private int pixelWidth, pixelHeight;
    //The array that stores individual pixels to be rendered on screen!
    private int[] pixels;
    //Array that stores depth buffer information, to sort what is rendered behind or on top on a complete frame
    private int[] zBuffer;
    //Array that stores lighting value for each pixel on screen
    private int[] lightMap;
    //Array that stores lighting block for each pixel on screen
    private int[] lightBlock;

    private int ambientColor = 0xff232323;
    private int zDepth = 0;
    private boolean processing = false;
    private int camX;
    private int camY;

    public Renderer(GameEngine ge)
    {
        pixelWidth = ge.getWidth();
        pixelHeight = ge.getHeight();
        pixels = ((DataBufferInt) ge.getWindow().getImage().getRaster().getDataBuffer()).getData();
        zBuffer = new int[pixels.length];
        lightMap = new int[pixels.length];
        lightBlock = new int[pixels.length];
    }

    /**
     * Clear the pixels array, rendering a full black image.
     * If Lightmap is set, it wil be set to default ambient color.
     */
    public void clear()
    {
        for (int i = 0; i < pixels.length; i++)
        {
            //efeito trippy!
            //p[i] += i;
            pixels[i] = 0;
            zBuffer[i] = 0;
            lightMap[i] = ambientColor;
            lightBlock[i] = 0;
        }
    }

    public void process()
    {
        processing = true;

        Collections.sort(imageRequest, new Comparator<ImageRequest>()
        {
            @Override
            public int compare(ImageRequest ir0, ImageRequest ir1)
            {
                if (ir0.zDepth < ir1.zDepth)
                {
                    return -1;
                }
                if (ir0.zDepth > ir1.zDepth)
                {
                    return 1;
                }

                return 0;
            }
        });

        for (int i = 0; i < imageRequest.size(); i++)
        {
            ImageRequest ir = imageRequest.get(i);
            setzDepth(ir.zDepth);
            drawImage(ir.image, ir.offX, ir.offY);
        }

        //Renderiza o Lighting
        for (int i = 0; i < lightRequest.size(); i++)
        {
            LightRequest lr = lightRequest.get(i);
            drawLightRequest(lr.light, lr.locX, lr.locY);
        }

        for (int i = 0; i < pixels.length; i++)
        {
            float r = ((lightMap[i] >> 16) & 0xff) / 255f;
            float g = ((lightMap[i] >> 8) & 0xff) / 255f;
            float b = (lightMap[i] & 0xff) / 255f;

            pixels[i] = ((int) (((pixels[i] >> 16) & 0xff) * r) << 16
                    | (int) (((pixels[i] >> 8) & 0xff) * g) << 8
                    | (int) ((pixels[i] & 0xff) * b));
        }

        imageRequest.clear();
        lightRequest.clear();
        processing = false;
    }

    public void setPixel(int x, int y, int value)
    {
        int alpha = ((value >> 24) & 0xff);

        if ((x < 0 || x >= pixelWidth || y < 0 || y >= pixelHeight) || alpha == 0)
        {
            return;
        }

        int index = x + y * pixelWidth;

        if (zBuffer[index] > zDepth)
        {
            return;
        }

        zBuffer[index] = zDepth;

        if (alpha == 255)
        {
            pixels[index] = value;
        } else
        {
            int pixelColor = pixels[x + y * pixelWidth];

            int newRed = ((pixelColor >> 16) & 0xff) - (int) ((((pixelColor >> 16) & 0xff) - ((value >> 16) & 0xff)) * (alpha / 255f));
            int newGreen = ((pixelColor >> 8) & 0xff) - (int) ((((pixelColor >> 8) & 0xff) - ((value >> 8) & 0xff)) * (alpha / 255f));
            int newBlue = (pixelColor & 0xff) - (int) (((pixelColor & 0xff) - (value & 0xff)) * (alpha / 255f));

            pixels[index] = (newRed << 16 | newGreen << 8 | newBlue);
        }
    }

    public void setLightMap(int x, int y, int value)
    {
        if (x < 0 || x >= pixelWidth || y < 0 || y >= pixelHeight)
        {
            return;
        }

        int baseColor = lightMap[x + y * pixelWidth];

        int maxRed = Math.max((baseColor >> 16) & 0xff, (value >> 16) & 0xff);
        int maxGreen = Math.max((baseColor >> 8) & 0xff, (value >> 8) & 0xff);
        int maxBlue = Math.max(baseColor & 0xff, value & 0xff);

        lightMap[x + y * pixelWidth] = (maxRed << 16 | maxGreen << 8 | maxBlue);
    }

    public void setLightBlock(int x, int y, int value)
    {
        if (x < 0 || x >= pixelWidth || y < 0 || y >= pixelHeight)
        {
            return;
        }

        if (zBuffer[x + y * pixelWidth] > zDepth)
        {
            return;
        }

        lightBlock[x + y * pixelWidth] = value;
    }

    public void drawText(String text, int offX, int offY, int color)
    {
        offX -= camX;
        offY -= camY;

        int offset = 0;

        for (int i = 0; i < text.length(); i++)
        {
            int unicode = text.codePointAt(i);

            for (int y = 0; y < font.getFontImage().getH(); y++)
            {
                for (int x = 0; x < font.getWidths()[unicode]; x++)
                {
                    if (font.getFontImage().getP()[(x + font.getOffsets()[unicode]) + y * font.getFontImage().getW()] == 0xffffffff)
                    {
                        setPixel(x + offX + offset, y + offY, color);
                    }
                }
            }
            offset += font.getWidths()[unicode];
        }
    }

    public void drawImage(Image image, int offX, int offY)
    {
        offX -= camX;
        offY -= camY;

        if (image.isAlpha() && !processing)
        {
            imageRequest.add(new ImageRequest(image, zDepth, offX, offY));
            return;
        }

        //Cancela renderização ao todo caso imagem esteja fora da tela
        if (offX < -image.getW())
        {
            return;
        }
        if (offY < -image.getH())
        {
            return;
        }
        if (offX >= pixelWidth)
        {
            return;
        }
        if (offY >= pixelHeight)
        {
            return;
        }

        int newX = 0;
        int newY = 0;
        int newWidth = image.getW();
        int newHeight = image.getH();

        //Renderiza apenas a parte visivel e descarta os pixels fora da tela, em
        //imagens parcialmente fora da tela
        if (offX < 0)
        {
            newX -= offX;
        }
        if (offY < 0)
        {
            newY -= offY;
        }
        if (newWidth + offX >= pixelWidth)
        {
            newWidth -= newWidth + offX - pixelWidth;
        }
        if (newHeight + offY >= pixelHeight)
        {
            newHeight -= newHeight + offY - pixelHeight;
        }

        for (int y = newY; y < newHeight; y++)
        {
            for (int x = newX; x < newWidth; x++)
            {
                setPixel(x + offX, y + offY, image.getP()[x + y * image.getW()]);
                setLightBlock(x + offX, y + offY, image.getLightBlock());
            }
        }
    }

    public void drawImageTile(ImageTile image, int offX, int offY, int tileX, int tileY)
    {
        offX -= camX;
        offY -= camY;

        if (image.isAlpha() && !processing)
        {
            imageRequest.add(new ImageRequest(image.getTileImage(tileX, tileY), zDepth, offX, offY));
            return;
        }

        //Cancela renderização ao todo caso imagem esteja fora da tela
        if (offX < -image.getTileW())
        {
            return;
        }
        if (offY < -image.getTileH())
        {
            return;
        }
        if (offX >= pixelWidth)
        {
            return;
        }
        if (offY >= pixelHeight)
        {
            return;
        }

        int newX = 0;
        int newY = 0;
        int newWidth = image.getTileW();
        int newHeight = image.getTileH();

        //Renderiza apenas a parte visivel e descarta os pixels fora da tela, em
        //imagens parcialmente fora da tela
        if (offX < 0)
        {
            newX -= offX;
        }
        if (offY < 0)
        {
            newY -= offY;
        }
        if (newWidth + offX >= pixelWidth)
        {
            newWidth -= newWidth + offX - pixelWidth;
        }
        if (newHeight + offY >= pixelHeight)
        {
            newHeight -= newHeight + offY - pixelHeight;
        }

        for (int y = newY; y < newHeight; y++)
        {
            for (int x = newX; x < newWidth; x++)
            {
                setPixel(x + offX, y + offY, image.getP()[(x + tileX * image.getTileW()) + (y + tileY * image.getTileH()) * image.getW()]);
                setLightBlock(x + offX, y + offY, image.getLightBlock());
            }
        }
    }

    public void drawRect(int offX, int offY, int width, int height, int color)
    {
        offX -= camX;
        offY -= camY;

        for (int y = 0; y <= height; y++)
        {
            setPixel(offX, y + offY, color);
            setPixel(offX + width, y + offY, color);
        }

        for (int x = 0; x <= width; x++)
        {
            setPixel(x + offX, offY, color);
            setPixel(x + offX, offY + height, color);
        }
    }

    public void drawFillRect(int offX, int offY, int width, int height, int color)
    {
        offX -= camX;
        offY -= camY;

        //Cancela renderização ao todo caso imagem esteja fora da tela
        if (offX < -width)
        {
            return;
        }
        if (offY < -height)
        {
            return;
        }
        if (offX >= pixelWidth)
        {
            return;
        }
        if (offY >= pixelHeight)
        {
            return;
        }

        int newX = 0;
        int newY = 0;
        int newWidth = width;
        int newHeight = height;

        //Renderiza apenas a parte visivel e descarta os pixels fora da tela, em
        //imagens parcialmente fora da tela
        if (offX < 0)
        {
            newX -= offX;
        }
        if (offY < 0)
        {
            newY -= offY;
        }
        if (newWidth + offX >= pixelWidth)
        {
            newWidth -= newWidth + offX - pixelWidth;
        }
        if (newHeight + offY >= pixelHeight)
        {
            newHeight -= newHeight + offY - pixelHeight;
        }

        for (int y = newY; y < newHeight; y++)
        {
            for (int x = newX; x < newWidth; x++)
            {
                setPixel(x + offX, y + offY, color);
            }
        }
    }

    public void drawLight(Light l, int offX, int offY)
    {
        lightRequest.add(new LightRequest(l, offX, offY));
    }

    private void drawLightRequest(Light l, int offX, int offY)
    {
        offX -= camX;
        offY -= camY;

        for (int i = 0; i <= l.getDiameter(); i++)
        {
            drawLightLine(l, l.getRadius(), l.getRadius(), i, 0, offX, offY);
            drawLightLine(l, l.getRadius(), l.getRadius(), i, l.getDiameter(), offX, offY);
            drawLightLine(l, l.getRadius(), l.getRadius(), 0, i, offX, offY);
            drawLightLine(l, l.getRadius(), l.getRadius(), l.getDiameter(), i, offX, offY);
        }
    }

    private void drawLightLine(Light l, int x0, int y0, int x1, int y1, int offX, int offY)
    {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;
        int e2;

        while (true)
        {
            int screenX = x0 - l.getRadius() + offX;
            int screenY = y0 - l.getRadius() + offY;

            if (screenX < 0 || screenX >= pixelWidth || screenY < 0 || screenY >= pixelHeight)
            {
                return;
            }

            int lightColor = l.getLightvalue(x0, y0);
            if (lightColor == 0)
            {
                return;
            }
            if (lightBlock[screenX + screenY * pixelWidth] == Light.FULL)
            {
                return;
            }

            setLightMap(screenX, screenY, lightColor);

            if (x0 == x1 && y0 == y1)
            {
                break;
            }

            e2 = 2 * err;

            if (e2 > -1 * dy)
            {
                err -= dy;
                x0 += sx;
            }

            if (e2 < dx)
            {
                err += dx;
                y0 += sy;
            }
        }
    }

    public int getzDepth()
    {
        return zDepth;
    }

    public void setzDepth(int zDepth)
    {
        this.zDepth = zDepth;
    }

    public int getAmbientColor()
    {
        return ambientColor;
    }

    public void setAmbientColor(int ambientColor)
    {
        this.ambientColor = ambientColor;
    }

    public int getCamX()
    {
        return camX;
    }

    public void setCamX(int camX)
    {
        this.camX = camX;
    }

    public int getCamY()
    {
        return camY;
    }

    public void setCamY(int camY)
    {
        this.camY = camY;
    }

}
