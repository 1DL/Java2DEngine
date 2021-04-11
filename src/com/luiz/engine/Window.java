/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.luiz.engine;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

/**
 *
 * @author Luiz
 */
public class Window {
    private JFrame frame;
    private BufferedImage image;
    private Canvas canvas;
    private BufferStrategy bufferedStrategy;
    private Graphics graphics;
    
    public Window(GameEngine ge) {
        //Buffered Image to be rendered, based on internal resolution
        image = new BufferedImage(ge.getWidth(), ge.getHeight(), BufferedImage.TYPE_INT_RGB);
        /*
        The canvas definition to render the buffered image. Dimension is the resolution 
        of the Canvas. It uses the scale value to multiply 
        and scale up/down the size of the canvas, based on internal resolution
        of the buffered image.
        */
        
        canvas = new Canvas();
        Dimension dimension = new Dimension((int) (ge.getWidth() * ge.getScale()), (int) (ge.getHeight() * ge.getScale()));
        canvas.setPreferredSize(dimension);
        canvas.setMaximumSize(dimension);
        canvas.setMinimumSize(dimension);
        
        //JFrame definition.
        frame = new JFrame(ge.getTitle());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(canvas, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        
        //Setting the canvas buffer strategy and pass to graphics to render
        canvas.createBufferStrategy(2);
        bufferedStrategy = canvas.getBufferStrategy();
        graphics = bufferedStrategy.getDrawGraphics();
               
    }
    
    public void update() {
        graphics.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight(), null);
        bufferedStrategy.show();
    }
    
    public BufferedImage getImage() {
        return image;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public JFrame getFrame() {
        return frame;
    }
}
