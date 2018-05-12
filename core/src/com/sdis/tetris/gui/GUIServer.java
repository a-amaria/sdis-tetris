package com.sdis.tetris.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.sdis.tetris.Buttons;
import com.sdis.tetris.Tetris;
import com.sdis.tetris.audio.SFX;
import com.sdis.tetris.audio.Song;
import com.sdis.tetris.network.ParseServersFile;
import com.sdis.tetris.network.Client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.*;

public class GUIServer extends GUIScreen{
    private final Stage stage = new Stage();
    private final Table table = new Table();
    Sprite background = new Sprite(new Texture(Gdx.files.internal("img/main_menu.png"), false));
    Sprite title = new Sprite(new Texture(Gdx.files.internal("img/main_title.png"), false));
    private final TextButton joinButton = new TextButton("Join_Server", Buttons.MenuButton);
    private final TextButton backButton = new TextButton("Main_Menu", Buttons.MenuButton);
    private final List<String> list;
    private Skin skin;
    private Skin skinv2;
    final ScrollPane scroll;
    ThreadPoolExecutor executor;
    ConcurrentHashMap<String,Integer> other_servers;

    private class JoinServer implements Runnable
    {
        @Override
        public void run()
        {
            parent.switchTo(new GUIMultiPlayer(parent));
        }
    }

    private class Back implements Runnable
    {
        @Override
        public void run()
        {
            parent.switchTo(new GUIMainMenu(parent));
        }
    }

    public GUIServer(Tetris paramParent) {
        super(paramParent, Song.THEME_A);
        background.setPosition(0,0);
        background.setSize((float)Gdx.graphics.getWidth(),(float)Gdx.graphics.getHeight());
        title.setPosition((float)Gdx.graphics.getWidth()/3.7f,(float)Gdx.graphics.getHeight()-title.getHeight()*2);
        title.setColor(175f,130f,80f,255f);
        skin  = new Skin(Gdx.files.internal("menu/myskin.json"), new TextureAtlas(Gdx.files.internal("menu/atlas.atlas")));
        skinv2  = new Skin(Gdx.files.internal("menu/menu.json"), new TextureAtlas(Gdx.files.internal("menu/menu.atlas")));
        list=new List<>(skin);
        list.setAlignment(1);
        LinkedBlockingQueue<Runnable> queue= new LinkedBlockingQueue<Runnable>();
        executor = new ThreadPoolExecutor(10, 20, 10, TimeUnit.SECONDS, queue);
        other_servers = new ConcurrentHashMap<>();

        executor.execute(new ParseServersFile(other_servers));
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String[] strings = new String[other_servers.size()];
        int i = 0;
        for (Map.Entry me: other_servers.entrySet()) {
            strings[i] = me.getKey()+ " " + me.getValue();
            i++;
        }
        list.setItems(strings);
        scroll = new ScrollPane(list, skinv2);
        table.add(list).size((float)Gdx.graphics.getWidth()/2, (float)Gdx.graphics.getHeight()/8).padBottom(10).row();
        table.add(scroll);
        table.row();
        table.add(joinButton).size((float)Gdx.graphics.getWidth()/2, (float)Gdx.graphics.getHeight()/8).padBottom(10).row();
        table.add(backButton).size((float)Gdx.graphics.getWidth()/2, (float)Gdx.graphics.getHeight()/8).padBottom(10).row();
        table.setFillParent(true);
        table.setVisible(true);
        stage.addActor(table);
        joinButton.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                audio.playSFX(SFX.HOVER);
                String selected = list.getSelected();
                String[] lineComponents = selected.split(" ");
                InetAddress adress = null;
                try {
                    adress = InetAddress.getByName(lineComponents[0]);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }

                int port = Integer.parseInt(lineComponents[1]);
                try {
                    Client.join_server("server1", adress, port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stage.addAction(Actions.sequence(Actions.moveTo(-480.0f, 0.0f, 0.5f), Actions.run(new JoinServer())));
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
            {
                if ( joinButton.isPressed())
                {
                    audio.playSFX(SFX.HOVER);
                }
            }
        });

        backButton.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                audio.playSFX(SFX.HOVER);
                stage.addAction(Actions.sequence(Actions.moveTo(-480.0f, 0.0f, 0.5f), Actions.run(new Back())));
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
            {
                if (! backButton.isPressed())
                {
                    audio.playSFX(SFX.SELECT);
                }
            }
        });
    }

    @Override
    public void render(float delta)
    {
        Gdx.gl.glClearColor( 1, 1, 1, 1 );
        Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

        stage.act();

        batch.begin();

        background.draw(batch);
        title.draw(batch);

        batch.end();
        stage.draw();
    }

    @Override
    public void resize(int width, int height)
    {
        stage.getViewport().update(width, height);
        //camera.position.set(camera.viewportWidth/2, camera.viewportHeight/2, 0);
    }

    @Override
    public void show()
    {
        Gdx.input.setInputProcessor(stage);

    }

    @Override
    public void dispose()
    {
        stage.dispose();
        skin.dispose();
    }
}