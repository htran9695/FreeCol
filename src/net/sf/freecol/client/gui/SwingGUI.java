/**
 *  Copyright (C) 2002-2015   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.client.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

import net.sf.freecol.client.ClientOptions;
import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.client.gui.animation.Animations;
import net.sf.freecol.client.gui.panel.ColonyPanel;
import net.sf.freecol.client.gui.panel.ColorChooserPanel;
import net.sf.freecol.client.gui.panel.CornerMapControls;
import net.sf.freecol.client.gui.panel.FreeColDialog;
import net.sf.freecol.client.gui.panel.LabourData.UnitData;
import net.sf.freecol.client.gui.panel.MapControls;
import net.sf.freecol.client.gui.panel.Parameters;
import net.sf.freecol.client.gui.panel.TradeRoutePanel;
import net.sf.freecol.client.gui.panel.Utility;
import net.sf.freecol.client.gui.plaf.FreeColLookAndFeel;
import net.sf.freecol.client.gui.video.VideoComponent;
import net.sf.freecol.client.gui.video.VideoListener;
import net.sf.freecol.common.FreeColException;
import net.sf.freecol.common.ServerInfo;
import net.sf.freecol.common.i18n.Messages;
import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.DiplomaticTrade;
import net.sf.freecol.common.model.Europe;
import net.sf.freecol.common.model.FoundingFather;
import net.sf.freecol.common.model.FreeColGameObject;
import net.sf.freecol.common.model.FreeColObject;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Goods;
import net.sf.freecol.common.model.GoodsType;
import net.sf.freecol.common.model.HighScore;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Location;
import net.sf.freecol.common.model.ModelMessage;
import net.sf.freecol.common.model.Monarch.MonarchAction;
import net.sf.freecol.common.model.Nation;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Settlement;
import net.sf.freecol.common.model.Specification;
import net.sf.freecol.common.model.StringTemplate;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.TileType;
import net.sf.freecol.common.model.TradeRoute;
import net.sf.freecol.common.model.TypeCountMap;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.UnitType;
import net.sf.freecol.common.model.WorkLocation;
import net.sf.freecol.common.option.BooleanOption;
import net.sf.freecol.common.option.LanguageOption;
import net.sf.freecol.common.option.LanguageOption.Language;
import net.sf.freecol.common.option.Option;
import net.sf.freecol.common.option.OptionGroup;
import net.sf.freecol.common.resources.ResourceManager;
import net.sf.freecol.common.resources.Video;

import static net.sf.freecol.common.util.StringUtils.lastPart;


/**
 * A wrapper providing functionality for the overall GUI using Java Swing.
 */
public class SwingGUI extends GUI {

    /** The graphics device. */
    private final GraphicsDevice graphicsDevice;

    /**
     * This is the TileViewer instance used to paint the map tiles
     * in the ColonyPanel and other panels.  It should not be scaled
     * along with the default MapViewer.
     */
    private TileViewer tileViewer;

    /**
     * The MapViewer instance used by canvas to paint the main map.
     * This does need to be scaled.
     */
    private MapViewer mapViewer;

    /** The canvas that implements much of the functionality. */
    private Canvas canvas;

    /** The map controls. */
    private MapControls mapControls;

    /** The splash. */
    private JWindow splash;


    /**
     * Create the GUI.
     *
     * @param freeColClient The <code>FreeColClient</code> for the game.
     * @param scaleFactor The scale factor for the GUI.
     */
    public SwingGUI(FreeColClient freeColClient, float scaleFactor) {
        super(freeColClient, scaleFactor);
        graphicsDevice = getGoodGraphicsDevice();
        logger.info("GUI constructed using scale factor " + scaleFactor);
    }


    // Simple accessors

    /**
     * Gets the canvas.
     *
     * @return the canvas
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Gets the tile image library.
     *
     * @return the tile image library
     */
    public ImageLibrary getTileImageLibrary() {
        return tileViewer.getImageLibrary();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#isWindowed()
     */
    @Override
    public boolean isWindowed() {
        return canvas.isWindowed();
    }

    // Initialization related methods

    /** 
     * Swing system and look-and-feel initialization.
     * 
     * @param fontName An optional font name to be used.
     */
    @Override
    public void installLookAndFeel(String fontName) throws FreeColException {
        FreeColLookAndFeel fclaf = new FreeColLookAndFeel();
        FreeColLookAndFeel.install(fclaf);
        Font font = FontLibrary.createMainFont(
            fontName, imageLibrary.getScaleFactor());
        FreeColLookAndFeel.installFont(font);
        Utility.initStyleContext(font);
    }

    /**
     * Quit the GUI.  All that is required is to exit the full screen.
     */
    @Override
    public void quit() throws Exception {
        if (canvas != null) {
            canvas.quit();
        }
    }

    /**
     * In game initializations.
     * Called from PreGameController.startGame().
     *
     * @param tile An initial <code>Tile</code> to select.
     */
    @Override
    public void initializeInGame(Tile tile) {
        canvas.initializeInGame();
        enableMapControls(freeColClient.getClientOptions()
            .getBoolean(ClientOptions.DISPLAY_MAP_CONTROLS));
        setSelectedTile(tile);
    }

    /**
     * Set up the mouse listeners for the canvas and map viewer.
     */
    @Override
    public void setupMouseListeners() {
        canvas.setupMouseListeners();
    }

    /**
     * Display the splash screen.
     *
     * @param splashStream A stream to read the splash image from.
     */
    @Override
    public void displaySplashScreen(final InputStream splashStream) {
        splash = null;
        if (splashStream == null) return;
        try {
            BufferedImage im = ImageIO.read(splashStream);
            splash = new JWindow(graphicsDevice.getDefaultConfiguration());
            splash.getContentPane().add(new JLabel(new ImageIcon(im)));
            splash.pack();
            Point start = splash.getLocation();
            DisplayMode dm = graphicsDevice.getDisplayMode();
            splash.setLocation(start.x + dm.getWidth()/2 - splash.getWidth() / 2,
                start.y + dm.getHeight()/2 - splash.getHeight() / 2);
            splash.setVisible(true);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Splash fail", e);
            splash = null;
        }
    }

    /**
     * Hide the splash screen.
     */
    @Override
    public void hideSplashScreen() {
        if (splash != null) {
            splash.setVisible(false);
            splash.dispose();
            splash = null;
        }
    }

    /**
     * Shows the <code>VideoPanel</code>.
     *
     * @param userMsg An optional user message.
     */
    @Override
    public void showOpeningVideo(final String userMsg) {
        canvas.closeMenus();
        final Video video = ResourceManager.getVideo("video.opening");
        boolean muteAudio = !freeColClient.getSoundController().canPlaySound();
        final VideoComponent vp = new VideoComponent(video, muteAudio);

        final class AbortListener implements ActionListener, KeyListener, MouseListener, VideoListener {

            private Timer t = null;

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e1) {
                execute();
            }

            @Override
            public void keyTyped(KeyEvent e2) {
            }

            @Override
            public void mouseClicked(MouseEvent e3) {
                execute();
            }

            @Override
            public void mouseEntered(MouseEvent e4) {
            }

            @Override
            public void mouseExited(MouseEvent e5) {
            }

            @Override
            public void mousePressed(MouseEvent e6) {
            }

            @Override
            public void mouseReleased(MouseEvent e7) {
            }

            @Override
            public void stopped() {
                execute();
            }

            @Override
            public void actionPerformed(ActionEvent ae8) {
                execute();
            }

            private void setTimer(Timer t1) {
                this.t = t1;
            }

            private void execute() {
                canvas.removeKeyListener(this);
                canvas.removeMouseListener(this);
                vp.removeMouseListener(this);
                //vp.removeVideoListener(this);
                vp.stop();
                canvas.remove(vp);
                if (t != null) {
                    t.stop();
                }
                playSound("sound.intro.general");
                showMainPanel(userMsg);
            }
        }
        AbortListener l = new AbortListener();
        vp.addMouseListener(l);
        //vp.addVideoListener(l);
        canvas.showVideoComponent(vp, l, l);
        vp.play();
        // Cortado applet is failing to quit when finished, make sure it
        // eventually gets kicked.  Change the magic number if we
        // change the opening video length.
        Timer t2 = new Timer(80000, l);
        l.setTimer(t2);
        t2.setRepeats(false);
        t2.start();
    }

    /**
     * Get a good screen device for starting FreeCol.
     *
     * @return A screen device, or null if none available
     *     (as in headless mode).
     */
    private static GraphicsDevice getGoodGraphicsDevice() {
        try {
            return MouseInfo.getPointerInfo().getDevice();
        } catch (HeadlessException he) {}

        try {
            final GraphicsEnvironment lge
                = GraphicsEnvironment.getLocalGraphicsEnvironment();
            return lge.getDefaultScreenDevice();
        } catch (HeadlessException he) {}

        FreeColClient.fatal("Could not find a GraphicsDevice!");
        return null;
    }

    /**
     * Starts the GUI by creating and displaying the GUI-objects.
     *
     * @param desiredWindowSize The desired size of the GUI window.
     */
    @Override
    public void startGUI(final Dimension desiredWindowSize) {
        final ClientOptions opts = freeColClient.getClientOptions();

        // Work around a Java 2D bug that seems to be X11 specific.
        // According to:
        //   http://www.oracle.com/technetwork/java/javase/index-142560.html
        //
        //   ``The use of pixmaps typically results in better
        //     performance. However, in certain cases, the opposite is true.''
        //
        // The standard workaround is to use -Dsun.java2d.pmoffscreen=false,
        // but this is too hard for some users, so provide an option to
        // do it easily.  However respect the initial value if present.
        //
        // Remove this if Java 2D is ever fixed.  DHYB.
        //
        final String pmoffscreen = "sun.java2d.pmoffscreen";
        BooleanOption usePixmaps
            = (BooleanOption) opts.getOption(ClientOptions.USE_PIXMAPS);
        String pmoffscreenValue = System.getProperty(pmoffscreen);
        if (pmoffscreenValue == null) {
            System.setProperty(pmoffscreen, usePixmaps.getValue().toString());
            logger.info(pmoffscreen + " using client option: "
                + usePixmaps.getValue());
        } else {
            usePixmaps.setValue(Boolean.valueOf(pmoffscreenValue));
            logger.info(pmoffscreen + " overrides client option: "
                + pmoffscreenValue);
        }
        usePixmaps.addPropertyChangeListener((PropertyChangeEvent e) -> {
                String newValue = e.getNewValue().toString();
                System.setProperty(pmoffscreen, newValue);
                logger.info("Set " + pmoffscreen + " to: " + newValue);
            });

        this.mapViewer = new MapViewer(freeColClient);
        this.canvas = new Canvas(freeColClient, graphicsDevice, this,
                                 desiredWindowSize, mapViewer);
        this.tileViewer = new TileViewer(freeColClient);

        // Now that there is a canvas, prepare for language changes.
        LanguageOption o = (LanguageOption)freeColClient.getClientOptions()
            .getOption(ClientOptions.LANGUAGE);
        o.addPropertyChangeListener((PropertyChangeEvent e) -> {
                Language language = (Language)e.getNewValue();
                logger.info("Set language to: " + language);
                if (Messages.AUTOMATIC.equalsIgnoreCase(language.getKey())) {
                    showInformationMessage("info.autodetectLanguageSelected");
                } else {
                    Locale l = language.getLocale();
                    Messages.loadMessageBundle(l);
                    Messages.loadModMessageBundle(l);
                    showInformationMessage(StringTemplate
                        .template("info.newLanguageSelected")
                        .addName("%language%", l.getDisplayName()));
                }
            });

        logger.info("GUI created.");
        logger.info("Starting in Move Units View Mode");
    }

    /**
     * Change the windowed mode.
     */
    @Override
    public void changeWindowedMode() {
        canvas.changeWindowedMode();
    }

    /**
     * Start the GUI for the map editor.
     */
    @Override
    public void startMapEditorGUI() {
        canvas.startMapEditorGUI();
    }


    // Non-trivial public routines.

    /**
     * Start/stop the goto path display.
     */
    @Override
    public void activateGotoPath() {
        Unit unit = getActiveUnit();

        // Action should be disabled if there is no active unit, but make sure
        if (unit == null) return;

        // Enter "goto mode" if not already activated; otherwise cancel it
        if (canvas.isGotoStarted()) {
            canvas.stopGoto();
        } else {
            canvas.startGoto();

            // Draw the path to the current mouse position, if the
            // mouse is over the screen; see also
            // CanvasMouseMotionListener.
            Point pt = canvas.getMousePosition();
            if (pt != null) {
                Tile tile = canvas.convertToMapTile(pt.x, pt.y);
                if (tile != null && unit.getTile() != tile) {
                    canvas.setGotoPath(unit.findPath(tile));
                }
            }
        }
    }

    /**
     * Stop the goto path display.
     */
    @Override
    public void clearGotoPath() {
        Unit unit = getActiveUnit();

        // Action should be disabled if there is no active unit, but make sure
        if (unit == null) return;
        canvas.stopGoto();
    }

    /**
     * Tells the map controls that a chat message was received.
     *
     * @param player The player who sent the chat message.
     * @param message The chat message.
     * @param privateChat 'true' if the message is a private one, 'false'
     *            otherwise.
     * @see GUIMessage
     */
    @Override
    public void displayChatMessage(Player player, String message,
                                   boolean privateChat) {
        canvas.displayChatMessage(new GUIMessage(
            player.getName() + ": " + message, player.getNationColor()));
    }

    /**
     * Refresh the GUI.
     */
    @Override
    public void refresh() {
        mapViewer.forceReposition();
        canvas.refresh();
    }

    /**
     * Refreshes the screen at the specified Tile.
     *
     * @param tile The <code>Tile</code> to refresh.
     */
    public void refreshTile(Tile tile) {
        if (tile.getX() >= 0 && tile.getY() >= 0) {
            canvas.repaint(mapViewer.calculateTileBounds(tile));
        }
    }

    /**
     * Reset the menu bar.
     */
    @Override
    public void resetMenuBar() {
        freeColClient.updateActions();
        canvas.resetMenuBar();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#resetMapZoom()
     */
    @Override
    protected void resetMapZoom() {
        super.resetMapZoom();
        mapViewer.resetMapScale();
        refresh();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#canZoomInMap()
     */
    @Override
    public boolean canZoomInMap() {
        return !mapViewer.isAtMaxMapScale();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#canZoomOutMap()
     */
    @Override
    public boolean canZoomOutMap() {
        return !mapViewer.isAtMinMapScale();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#zoomInMap()
     */
    @Override
    public void zoomInMap() {
        super.zoomInMap();
        mapViewer.increaseMapScale();
        refresh();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#zoomOutMap()
     */
    @Override
    public void zoomOutMap() {
        super.zoomOutMap();
        mapViewer.decreaseMapScale();
        refresh();
    }

    /**
     * Set the active unit.
     *
     * @param unit The <code>Unit</code> to activate.
     * @return True if the focus was set.
     */
    @Override
    public boolean setActiveUnit(Unit unit) {
        boolean result = mapViewer.setActiveUnit(unit);
        updateMapControls();
        updateMenuBar();
        if (unit != null && !freeColClient.getMyPlayer().owns(unit)) {
            canvas.refresh();
        }
        return result;
    }

    /**
     * Update the menu bar.
     *
     * Always update the actions first so that the enabled/disabled
     * state is correct.
     */
    @Override
    public void updateMenuBar() {
        freeColClient.updateActions();
        canvas.updateMenuBar();
    }


    // Animation handling

    /**
     * Require the given tile to be in the onScreen()-area.
     *
     * @param tile The <code>Tile</code> to check.
     * @return True if the focus was set.
     */
    public boolean requireFocus(Tile tile) {
        // Account for the ALWAYS_CENTER client option.
        boolean required = freeColClient.getClientOptions()
            .getBoolean(ClientOptions.ALWAYS_CENTER);
        if ((required && tile != getFocus()) || !mapViewer.onScreen(tile)) {
            setFocusImmediately(tile);
            return true;
        }
        return false;
    }
        
    /**
     * Animate a unit attack.
     *
     * @param attacker The attacking <code>Unit</code>.
     * @param defender The defending <code>Unit</code>.
     * @param attackerTile The <code>Tile</code> to show the attacker on.
     * @param defenderTile The <code>Tile</code> to show the defender on.
     * @param success Did the attack succeed?
     */
    @Override
    public void animateUnitAttack(Unit attacker, Unit defender,
                                  Tile attackerTile, Tile defenderTile,
                                  boolean success) {
        requireFocus(attackerTile);
        Animations.unitAttack(freeColClient, attacker, defender,
                              attackerTile, defenderTile, success);
    }

    /**
     * Animate a unit move.
     *
     * @param unit The <code>Unit</code> that is moving.
     * @param srcTile The <code>Tile</code> the unit starts at.
     * @param dstTile The <code>Tile</code> the unit moves to.
     */
    @Override
    public void animateUnitMove(Unit unit, Tile srcTile, Tile dstTile) {
        requireFocus(srcTile);
        Animations.unitMove(freeColClient, unit, srcTile, dstTile);
    }


    // MapControls handling

    /**
     * Enable the map controls.
     *
     * Called from the MapControlsAction.
     *
     * @param enable If true then enable.
     */
    @Override
    public void enableMapControls(boolean enable) {
        // Always instantiate in game.
        if (enable && mapControls == null) {
            String className = freeColClient.getClientOptions()
                .getString(ClientOptions.MAP_CONTROLS);
            try {
                final String panelName = "net.sf.freecol.client.gui.panel."
                    + lastPart(className, ".");
                Class<?> controls = Class.forName(panelName);
                mapControls = (MapControls)controls
                    .getConstructor(FreeColClient.class)
                    .newInstance(freeColClient);
                logger.info("Instantiated " + panelName);
            } catch (Exception e) {
                logger.log(Level.INFO, "Fallback to CornerMapControls from "
                    + className, e);
                mapControls = new CornerMapControls(freeColClient);
            }
            if (mapControls != null) {
                mapControls.addToComponent(canvas);
                mapControls.update();
            }
        } else if (!enable && mapControls != null) {
            mapControls.removeFromComponent(canvas);
            mapControls = null;
        }
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#updateMapControls()
     */
    @Override
    public void updateMapControls() {
        if (mapControls != null) mapControls.update();
    }

    /**
     * Update map controls in canvas.
     */
    public void updateMapControlsInCanvas() {
        if (mapControls == null) return;
        mapControls.removeFromComponent(canvas);
        mapControls.addToComponent(canvas);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#zoomInMapControls()
     */
    @Override
    public void zoomInMapControls() {
        if (mapControls == null) return;
        mapControls.zoomIn();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#zoomOutMapControls()
     */
    @Override
    public void zoomOutMapControls() {
        if (mapControls == null) return;
        mapControls.zoomOut();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#canZoomInMapControls()
     */
    @Override
    public boolean canZoomInMapControls() {
        return mapControls != null && mapControls.canZoomInMapControls();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#canZoomOutMapControls()
     */
    @Override
    public boolean canZoomOutMapControls() {
        return mapControls != null && mapControls.canZoomOutMapControls();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#miniMapToggleViewControls()
     */
    @Override
    public void miniMapToggleViewControls() {
        if (mapControls == null) return;
        mapControls.toggleView();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#miniMapToggleFogOfWarControls()
     */
    @Override
    public void miniMapToggleFogOfWarControls() {
        if (mapControls == null) return;
        mapControls.toggleFogOfWar();
    }


    // Dialogs that return values

    /**
     * Simple modal confirmation dialog.
     *
     * @param textKey A string to use as the message key.
     * @param okKey A key for the "ok" button.
     * @param cancelKey A key for the "cancel" button.
     * @return True if the "ok" button was selected.
     */
    @Override
    public boolean confirm(String textKey, String okKey, String cancelKey) {
        return canvas.showConfirmDialog(null, Messages.message(textKey),
            null, okKey, cancelKey);
    }

    /**
     * General modal confirmation dialog.
     *
     * @param tile An optional <code>Tile</code> to expose.
     * @param template The <code>StringTemplate</code> explaining the choice.
     * @param okKey A key for the "ok" button.
     * @param cancelKey A key for the "cancel" button.
     * @return True if the "ok" button was selected.
     */
    @Override
    public boolean confirm(Tile tile, StringTemplate template,
                           String okKey, String cancelKey) {
        return canvas.showConfirmDialog(tile,
            Utility.localizedTextArea(template), null, okKey, cancelKey);
    }

    /**
     * General modal confirmation dialog.
     *
     * @param tile An optional <code>Tile</code> to expose.
     * @param template The <code>StringTemplate</code> explaining the choice.
     * @param unit An optional unit to make an icon for the dialog from.
     * @param okKey A key for the "ok" button.
     * @param cancelKey A key for the "cancel" button.
     * @return True if the "ok" button was selected.
     */
    @Override
    public boolean confirm(Tile tile, StringTemplate template, Unit unit,
                           String okKey, String cancelKey) {
        return canvas.showConfirmDialog(tile,
            Utility.localizedTextArea(template),
            new ImageIcon(imageLibrary.getUnitImage(unit)),
            okKey, cancelKey);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#confirm(net.sf.freecol.common.model.Tile, net.sf.freecol.common.model.StringTemplate, net.sf.freecol.common.model.Settlement, java.lang.String, java.lang.String)
     */
    @Override
    public boolean confirm(Tile tile, StringTemplate template,
                           Settlement settlement,
                           String okKey, String cancelKey) {
        return canvas.showConfirmDialog(tile,
            Utility.localizedTextArea(template),
            new ImageIcon(imageLibrary.getSettlementImage(settlement)),
            okKey, cancelKey);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#confirm(net.sf.freecol.common.model.Tile, net.sf.freecol.common.model.StringTemplate, net.sf.freecol.common.model.GoodsType, java.lang.String, java.lang.String)
     */
    @Override
    public boolean confirm(Tile tile, StringTemplate template,
                           GoodsType goodsType,
                           String okKey, String cancelKey) {
        return canvas.showConfirmDialog(tile,
            Utility.localizedTextArea(template),
            new ImageIcon(imageLibrary.getIconImage(goodsType)),
            okKey, cancelKey);
    }

    /**
     * Confirm declaration of independence.
     *
     * @return A list of new nation and country names.
     */
    @Override
    public List<String> confirmDeclaration() {
        return canvas.showConfirmDeclarationDialog();
    }

    /**
     * General modal choice dialog.
     *
     * @param tile An optional <code>Tile</code> to expose.
     * @param explain An object explaining the choice.
     * @param cancelKey A key for the "cancel" button.
     * @param choices A list a <code>ChoiceItem</code>s to choose from.
     * @return The selected value of the selected <code>ChoiceItem</code>,
     *     or null if cancelled.
     */
    @Override
    public <T> T getChoice(Tile tile, Object explain,
                           String cancelKey, List<ChoiceItem<T>> choices) {
        return canvas.showChoiceDialog(tile, explain,
            null, cancelKey, choices);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#getChoice(net.sf.freecol.common.model.Tile, java.lang.Object, net.sf.freecol.common.model.Unit, java.lang.String, java.util.List)
     */
    @Override
    public <T> T getChoice(Tile tile, Object explain, Unit unit,
                           String cancelKey, List<ChoiceItem<T>> choices) {
        return canvas.showChoiceDialog(tile, explain,
            new ImageIcon(imageLibrary.getUnitImage(unit)),
            cancelKey, choices);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#getChoice(net.sf.freecol.common.model.Tile, java.lang.Object, net.sf.freecol.common.model.Settlement, java.lang.String, java.util.List)
     */
    @Override
    public <T> T getChoice(Tile tile, Object explain, Settlement settlement,
                           String cancelKey, List<ChoiceItem<T>> choices) {
        return canvas.showChoiceDialog(tile, explain,
            new ImageIcon(imageLibrary.getSettlementImage(settlement)),
            cancelKey, choices);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#getChoice(net.sf.freecol.common.model.Tile, java.lang.Object, net.sf.freecol.common.model.GoodsType, java.lang.String, java.util.List)
     */
    @Override
    public <T> T getChoice(Tile tile, Object explain, GoodsType goodsType,
                           String cancelKey, List<ChoiceItem<T>> choices) {
        return canvas.showChoiceDialog(tile, explain,
            new ImageIcon(imageLibrary.getIconImage(goodsType)),
            cancelKey, choices);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#getChoice(net.sf.freecol.common.model.Tile, java.lang.Object, net.sf.freecol.common.model.Nation, java.lang.String, java.util.List)
     */
    @Override
    public <T> T getChoice(Tile tile, Object explain, Nation nation,
                           String cancelKey, List<ChoiceItem<T>> choices) {
        return canvas.showChoiceDialog(tile, explain,
            new ImageIcon(imageLibrary.getMiscIconImage(nation)),
            cancelKey, choices);
    }

    /**
     * General modal string input dialog.
     *
     * @param tile An optional <code>Tile</code> to expose.
     * @param template A <code>StringTemplate</code> explaining the choice.
     * @param defaultValue The default value to show initially.
     * @param okKey A key for the "ok" button.
     * @param cancelKey A key for the "cancel" button.
     * @return The chosen value.
     */
    @Override
    public String getInput(Tile tile, StringTemplate template,
                           String defaultValue,
                           String okKey, String cancelKey) {
        return canvas.showInputDialog(tile, template, defaultValue,
                                      okKey, cancelKey);
    }


    // Trivial delegations to Canvas

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#closeMainPanel()
     */
    @Override
    public void closeMainPanel() {
        canvas.closeMainPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#closeMenus()
     */
    @Override
    public void closeMenus() {
        canvas.closeMenus();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#closeStatusPanel()
     */
    @Override
    public void closeStatusPanel() {
        canvas.closeStatusPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#containsInGameComponents()
     */
    @Override
    public boolean containsInGameComponents() {
        return canvas.containsInGameComponents();
    }

    /**
     * Dialog remove.
     *
     * @param fcd the fcd
     */
    public void dialogRemove(FreeColDialog<?> fcd) {
        canvas.dialogRemove(fcd);
    }

    /**
     * Show the appropriate panel for an object.
     *
     * TODO: Improve OO.
     *
     * @param fco The <code>FreeColObject</code> to display.
     */
    public void displayObject(FreeColObject fco) {
        if (fco instanceof Colony) {
            canvas.showColonyPanel((Colony)fco, null);
        } else if (fco instanceof Europe) {
            canvas.showEuropePanel();
        } else if (fco instanceof IndianSettlement) {
            canvas.showIndianSettlementPanel((IndianSettlement)fco);
        } else if (fco instanceof Tile) {
            setFocus((Tile)fco);
        } else if (fco instanceof Unit) {
            Location loc = ((Unit)fco).up();
            if (loc instanceof Colony) {
                canvas.showColonyPanel((Colony)loc, (Unit)fco);
            } else {
                displayObject((FreeColObject)loc);
            }
        } else if (fco instanceof WorkLocation) {
            canvas.showColonyPanel(((WorkLocation)fco).getColony(), null);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#getLoadingSavegameInfo()
     */
    @Override
    public LoadingSavegameInfo getLoadingSavegameInfo() {
        return canvas.getLoadingSavegameDialog().getInfo();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#isClientOptionsDialogShowing()
     */
    @Override
    public boolean isClientOptionsDialogShowing() {
        return canvas!=null && canvas.isClientOptionsDialogShowing();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#isMapboardActionsEnabled()
     */
    @Override
    public boolean isMapboardActionsEnabled() {
        return canvas!=null && canvas.isMapboardActionsEnabled();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#isShowingSubPanel()
     */
    @Override
    public boolean isShowingSubPanel() {
        return canvas!=null && canvas.isShowingSubPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#paintImmediatelyCanvasIn(java.awt.Rectangle)
     */
    @Override
    public void paintImmediatelyCanvasIn(Rectangle rectangle) {
        canvas.paintImmediately(rectangle);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#paintImmediatelyCanvasInItsBounds()
     */
    @Override
    public void paintImmediatelyCanvasInItsBounds() {
        canvas.paintImmediately(canvas.getBounds());
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#refreshPlayersTable()
     */
    @Override
    public void refreshPlayersTable() {
        canvas.refreshPlayersTable();
    }

    /**
     * Removes the from canvas.
     *
     * @param component the component
     */
    public void removeFromCanvas(Component component) {
        canvas.remove(component);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#removeInGameComponents()
     */
    @Override
    public void removeInGameComponents() {
        canvas.removeInGameComponents();
    }

    /**
     * Removes the trade route panel.
     *
     * @param panel the panel
     */
    public void removeTradeRoutePanel(TradeRoutePanel panel) {
        canvas.removeTradeRoutePanel(panel);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#requestFocusForSubPanel()
     */
    @Override
    public void requestFocusForSubPanel() {
        canvas.getShowingSubPanel().requestFocus();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#requestFocusInWindow()
     */
    @Override
    public boolean requestFocusInWindow() {
        return canvas.requestFocusInWindow();
    }

    /**
     * Restore saved size.
     *
     * @param comp the comp
     * @param w the w
     * @param h the h
     */
    public void restoreSavedSize(Component comp, int w, int h) {
        canvas.restoreSavedSize(comp, new Dimension(w, h));
    }

    /**
     * Restore saved size.
     *
     * @param comp the comp
     * @param size the size
     */
    public void restoreSavedSize(Component comp, Dimension size) {
        canvas.restoreSavedSize(comp, size);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#returnToTitle()
     */
    @Override
    public void returnToTitle() {
        canvas.returnToTitle();
        playSound("sound.intro.general");
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showAboutPanel()
     */
    @Override
    public void showAboutPanel() {
        canvas.showAboutPanel();
    }

    /**
     * Show build queue panel.
     *
     * @param colony the colony
     */
    public void showBuildQueuePanel(Colony colony) {
        canvas.showBuildQueuePanel(colony);
    }

    /**
     * Show build queue panel.
     *
     * @param colony the colony
     * @param callBack the call back
     */
    public void showBuildQueuePanel(Colony colony, Runnable callBack) {
        canvas.showBuildQueuePanel(colony, callBack);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showCaptureGoodsDialog(net.sf.freecol.common.model.Unit, java.util.List, net.sf.freecol.client.gui.DialogHandler)
     */
    @Override
    public void showCaptureGoodsDialog(final Unit unit, List<Goods> gl,
                                       DialogHandler<List<Goods>> handler) {
        canvas.showCaptureGoodsDialog(unit, gl, handler);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showChatPanel()
     */
    @Override
    public void showChatPanel() {
        canvas.showChatPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showChooseFoundingFatherDialog(java.util.List, net.sf.freecol.client.gui.DialogHandler)
     */
    @Override
    public void showChooseFoundingFatherDialog(final List<FoundingFather> ffs,
                DialogHandler<FoundingFather> handler) {
        canvas.showChooseFoundingFatherDialog(ffs, handler);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showClientOptionsDialog()
     */
    @Override
    public void showClientOptionsDialog() {
        OptionGroup group = null;
        try {
            group = canvas.showClientOptionsDialog();
        } finally {
            if (group != null) {
                resetMenuBar();
                // Immediately redraw the minimap if that was updated.
                updateMapControls();
            }
        }
        if (!freeColClient.isInGame()) showMainPanel(null);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showForeignColony(net.sf.freecol.common.model.Settlement)
     */
    @Override
    protected void showForeignColony(Settlement settlement) {
        canvas.showForeignColony(settlement);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showColonyPanel(net.sf.freecol.common.model.Colony, net.sf.freecol.common.model.Unit)
     */
    @Override
    public void showColonyPanel(Colony colony, Unit unit) {
        canvas.showColonyPanel(colony, unit);
    }

    /**
     * Show colony panel 2.
     *
     * @param colony the colony
     * @param unit the unit
     * @return the colony panel
     */
    public ColonyPanel showColonyPanel2(Colony colony, Unit unit) {
        return canvas.showColonyPanel(colony, unit);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showColopediaPanel(java.lang.String)
     */
    @Override
    public void showColopediaPanel(String nodeId) {
        canvas.showColopediaPanel(nodeId);
    }

    /**
     * Show color chooser panel.
     *
     * @param al the al
     * @return the color chooser panel
     */
    public ColorChooserPanel showColorChooserPanel(ActionListener al) {
        return canvas.showColorChooserPanel(al);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showCompactLabourReport()
     */
    @Override
    public void showCompactLabourReport() {
        canvas.showCompactLabourReport();
    }

    /**
     * Show compact labour report.
     *
     * @param unitData the unit data
     */
    public void showCompactLabourReport(UnitData unitData) {
        canvas.showCompactLabourReport(unitData);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showDeclarationPanel()
     */
    @Override
    public void showDeclarationPanel() {
        canvas.showDeclarationPanel();
    }

    /**
     * Display a difficulty dialog allowing only viewing of game options.
     *
     * @return The resulting <code>OptionGroup</code>.
     */
    @Override
    public OptionGroup showDifficultyDialog() {
        Game game = freeColClient.getGame();
        Specification spec = game.getSpecification();
        return canvas.showDifficultyDialog(spec,
            spec.getDifficultyOptionGroup(), false);
    }

    /**
     * Show difficulty dialog.
     *
     * @param spec the spec
     * @param group the group
     * @return the option group
     */
    public OptionGroup showDifficultyDialog(Specification spec,
                                            OptionGroup group) {
        return canvas.showDifficultyDialog(spec, group, group.isEditable());
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showDumpCargoDialog(net.sf.freecol.common.model.Unit, net.sf.freecol.client.gui.DialogHandler)
     */
    @Override
    public void showDumpCargoDialog(Unit unit,
                                    DialogHandler<List<Goods>> handler) {
        canvas.showDumpCargoDialog(unit, handler);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showEditOptionDialog(net.sf.freecol.common.option.Option)
     */
    @Override
    public boolean showEditOptionDialog(Option option) {
        return canvas.showEditOptionDialog(option);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showEmigrationDialog(net.sf.freecol.common.model.Player, boolean, net.sf.freecol.client.gui.DialogHandler)
     */
    @Override
    public void showEmigrationDialog(final Player player,
                                     final boolean fountainOfYouth,
                                     DialogHandler<Integer> handler) {
        canvas.showEmigrationDialog(player, fountainOfYouth, handler);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showEndTurnDialog(java.util.List, net.sf.freecol.client.gui.DialogHandler)
     */
    @Override
    public void showEndTurnDialog(final List<Unit> units,
                                  DialogHandler<Boolean> handler) {
        canvas.showEndTurnDialog(units, handler);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showErrorMessage(net.sf.freecol.common.model.StringTemplate)
     */
    @Override
    public void showErrorMessage(StringTemplate template) {
        canvas.showErrorMessage(Messages.message(template));
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showErrorMessage(java.lang.String)
     */
    @Override
    public void showErrorMessage(String messageId) {
        canvas.showErrorMessage(messageId);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showErrorMessage(java.lang.String, java.lang.String)
     */
    @Override
    public void showErrorMessage(String messageID, String message) {
        canvas.showErrorMessage(messageID, message);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showEuropePanel()
     */
    @Override
    public void showEuropePanel() {
        canvas.showEuropePanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showEventPanel(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void showEventPanel(String header, String image, String footer) {
        canvas.showEventPanel(header, image, footer);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showFindSettlementPanel()
     */
    @Override
    public void showFindSettlementPanel() {
        canvas.showFindSettlementPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showGameOptionsDialog(boolean, boolean)
     */
    @Override
    public OptionGroup showGameOptionsDialog(boolean editable, boolean custom) {
        return canvas.showGameOptionsDialog(editable, custom);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showHighScoresPanel(java.lang.String, java.util.List)
     */
    @Override
    public void showHighScoresPanel(String messageId, List<HighScore> scores) {
        canvas.showHighScoresPanel(messageId, scores);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showIndianSettlementPanel(net.sf.freecol.common.model.IndianSettlement)
     */
    @Override
    public void showIndianSettlementPanel(IndianSettlement indianSettlement) {
        canvas.showIndianSettlementPanel(indianSettlement);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showInformationMessage(java.lang.String)
     */
    @Override
    public void showInformationMessage(String messageId) {
        super.showInformationMessage(messageId);
        canvas.showInformationMessage(null, null, null, StringTemplate.key(messageId));
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showInformationMessage(net.sf.freecol.common.model.StringTemplate)
     */
    @Override
    public void showInformationMessage(StringTemplate template) {
        super.showInformationMessage(template);
        canvas.showInformationMessage(null, null, null, template);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showInformationMessage(net.sf.freecol.common.model.Settlement, net.sf.freecol.common.model.StringTemplate)
     */
    @Override
    public void showInformationMessage(Settlement displayObject,
                                       StringTemplate template) {
        super.showInformationMessage(displayObject, template);
        ImageIcon icon = null;
        Tile tile = null;
        if(displayObject != null) {
            icon = new ImageIcon(imageLibrary.getSettlementImage(displayObject));
            tile = displayObject.getTile();
        }
        canvas.showInformationMessage(displayObject, tile, icon, template);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showInformationMessage(net.sf.freecol.common.model.Unit, net.sf.freecol.common.model.StringTemplate)
     */
    @Override
    public void showInformationMessage(Unit displayObject,
                                       StringTemplate template) {
        super.showInformationMessage(displayObject, template);
        ImageIcon icon = null;
        Tile tile = null;
        if(displayObject != null) {
            icon = new ImageIcon(imageLibrary.getUnitImage(displayObject));
            tile = displayObject.getTile();
        }
        canvas.showInformationMessage(displayObject, tile, icon, template);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showInformationMessage(net.sf.freecol.common.model.Tile, net.sf.freecol.common.model.StringTemplate)
     */
    @Override
    public void showInformationMessage(Tile displayObject,
                                       StringTemplate template) {
        super.showInformationMessage(displayObject, template);
        canvas.showInformationMessage(displayObject, displayObject, null, template);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showInformationMessage(net.sf.freecol.common.model.FreeColObject, java.lang.String)
     */
    @Override
    public void showInformationMessage(FreeColObject displayObject,
                                       String messageId) {
        super.showInformationMessage(displayObject, messageId);
        canvas.showInformationMessage(displayObject, StringTemplate.key(messageId));
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showInformationMessage(net.sf.freecol.common.model.FreeColObject, net.sf.freecol.common.model.StringTemplate)
     */
    @Override
    public void showInformationMessage(FreeColObject displayObject,
                                       StringTemplate template) {
        super.showInformationMessage(displayObject, template);
        canvas.showInformationMessage(displayObject, template);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showLoadDialog(java.io.File)
     */
    @Override
    public File showLoadDialog(File directory) {
        return canvas.showLoadDialog(directory, null);
    }

    /**
     * Show load dialog.
     *
     * @param directory the directory
     * @param fileFilters the file filters
     * @return the file
     */
    public File showLoadDialog(File directory, FileFilter[] fileFilters) {
        return canvas.showLoadDialog(directory, fileFilters);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showLoadingSavegameDialog(boolean, boolean)
     */
    @Override
    public boolean showLoadingSavegameDialog(boolean publicServer,
                                             boolean singlePlayer) {
        return canvas.showLoadingSavegameDialog(publicServer, singlePlayer);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showLogFilePanel()
     */
    @Override
    public void showLogFilePanel() {
        canvas.showLogFilePanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showMainPanel(java.lang.String)
     */
    @Override
    public void showMainPanel(String userMsg) {
        canvas.showMainPanel(userMsg);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showMapGeneratorOptionsDialog(boolean)
     */
    @Override
    public OptionGroup showMapGeneratorOptionsDialog(boolean editable) {
        return canvas.showMapGeneratorOptionsDialog(editable);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showMapSizeDialog()
     */
    @Override
    public Dimension showMapSizeDialog() {
        return canvas.showMapSizeDialog();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showModelMessages(java.util.List)
     */
    @Override
    public void showModelMessages(List<ModelMessage> modelMessages) {
        canvas.showModelMessages(modelMessages);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showMonarchDialog(net.sf.freecol.common.model.Monarch.MonarchAction, net.sf.freecol.common.model.StringTemplate, java.lang.String, net.sf.freecol.client.gui.DialogHandler)
     */
    @Override
    public void showMonarchDialog(final MonarchAction action,
                                  StringTemplate template, String monarchKey,
                                  DialogHandler<Boolean> handler) {
        canvas.showMonarchDialog(action, template, monarchKey, handler);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showNamingDialog(net.sf.freecol.common.model.StringTemplate, java.lang.String, net.sf.freecol.common.model.Unit, net.sf.freecol.client.gui.DialogHandler)
     */
    @Override
    public void showNamingDialog(StringTemplate template,
                                      final String defaultName,
                                      final Unit unit,
                                      DialogHandler<String> handler) {
        canvas.showNamingDialog(template, defaultName, unit, handler);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showFirstContactDialog(net.sf.freecol.common.model.Player, net.sf.freecol.common.model.Player, net.sf.freecol.common.model.Tile, int, net.sf.freecol.client.gui.DialogHandler)
     */
    @Override
    public void showFirstContactDialog(final Player player, final Player other,
                                       final Tile tile, int settlementCount,
                                       DialogHandler<Boolean> handler) {
        canvas.showFirstContactDialog(player, other, tile, settlementCount,
                                      handler);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showNegotiationDialog(net.sf.freecol.common.model.FreeColGameObject, net.sf.freecol.common.model.FreeColGameObject, net.sf.freecol.common.model.DiplomaticTrade, net.sf.freecol.common.model.StringTemplate)
     */
    @Override
    public DiplomaticTrade showNegotiationDialog(FreeColGameObject our,
                                                     FreeColGameObject other,
                                                     DiplomaticTrade agreement,
                                                     StringTemplate comment) {
        return canvas.showNegotiationDialog(our, other, agreement, comment);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showNewPanel()
     */
    @Override
    public void showNewPanel() {
        canvas.showNewPanel(null);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showNewPanel(net.sf.freecol.common.model.Specification)
     */
    @Override
    public void showNewPanel(Specification specification) {
        canvas.showNewPanel(specification);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showSpyColonyPanel(net.sf.freecol.common.model.Tile, java.lang.Runnable)
     */
    @Override
    public void showSpyColonyPanel(final Tile tile, Runnable callback) {
        ColonyPanel panel = canvas.showSpyColonyPanel(tile);
        panel.addClosingCallback(callback);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showParametersDialog()
     */
    @Override
    public Parameters showParametersDialog() {
        return canvas.showParametersDialog();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showPreCombatDialog(net.sf.freecol.common.model.Unit, net.sf.freecol.common.model.FreeColGameObject, net.sf.freecol.common.model.Tile)
     */
    @Override
    public boolean showPreCombatDialog(Unit attacker,
                                       FreeColGameObject defender, Tile tile) {
        return canvas.showPreCombatDialog(attacker, defender, tile);
    }

    /**
     * Show purchase panel.
     */
    public void showPurchasePanel() {
        canvas.showPurchasePanel();
    }

    /**
     * Show recruit panel.
     */
    public void showRecruitPanel() {
        canvas.showRecruitPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showReportCargoPanel()
     */
    @Override
    public void showReportCargoPanel() {
        canvas.showReportCargoPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showReportColonyPanel()
     */
    @Override
    public void showReportColonyPanel() {
        canvas.showReportColonyPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showReportContinentalCongressPanel()
     */
    @Override
    public void showReportContinentalCongressPanel() {
        canvas.showReportContinentalCongressPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showReportEducationPanel()
     */
    @Override
    public void showReportEducationPanel() {
        canvas.showReportEducationPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showReportExplorationPanel()
     */
    @Override
    public void showReportExplorationPanel() {
        canvas.showReportExplorationPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showReportForeignAffairPanel()
     */
    @Override
    public void showReportForeignAffairPanel() {
        canvas.showReportForeignAffairPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showReportHistoryPanel()
     */
    @Override
    public void showReportHistoryPanel() {
        canvas.showReportHistoryPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showReportIndianPanel()
     */
    @Override
    public void showReportIndianPanel() {
        canvas.showReportIndianPanel();
    }

    /**
     * Show report labour detail panel.
     *
     * @param unitType the unit type
     * @param data the data
     * @param unitCount the unit count
     * @param colonies the colonies
     */
    public void showReportLabourDetailPanel(UnitType unitType,
            Map<UnitType, Map<Location, Integer>> data,
            TypeCountMap<UnitType> unitCount, List<Colony> colonies) {
        canvas.showReportLabourDetailPanel(unitType, data, unitCount,
                                           colonies);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showReportLabourPanel()
     */
    @Override
    public void showReportLabourPanel() {
        canvas.showReportLabourPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showReportMilitaryPanel()
     */
    @Override
    public void showReportMilitaryPanel() {
        canvas.showReportMilitaryPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showReportNavalPanel()
     */
    @Override
    public void showReportNavalPanel() {
        canvas.showReportNavalPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showReportProductionPanel()
     */
    @Override
    public void showReportProductionPanel() {
        canvas.showReportProductionPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showReportReligiousPanel()
     */
    @Override
    public void showReportReligiousPanel() {
        canvas.showReportReligiousPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showReportRequirementsPanel()
     */
    @Override
    public void showReportRequirementsPanel() {
        canvas.showReportRequirementsPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showReportTradePanel()
     */
    @Override
    public void showReportTradePanel() {
        canvas.showReportTradePanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showReportTurnPanel(java.util.List)
     */
    @Override
    public void showReportTurnPanel(List<ModelMessage> messages) {
        canvas.showReportTurnPanel(messages);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showSaveDialog(java.io.File, java.lang.String)
     */
    @Override
    public File showSaveDialog(File directory, String defaultName) {
        return canvas.showSaveDialog(directory, null, defaultName);
    }

    /**
     * Show save dialog.
     *
     * @param directory the directory
     * @param fileFilters the file filters
     * @param defaultName the default name
     * @return the file
     */
    public File showSaveDialog(File directory, FileFilter[] fileFilters,
                               String defaultName) {
        return canvas.showSaveDialog(directory, fileFilters, defaultName);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showScaleMapSizeDialog()
     */
    @Override
    public Dimension showScaleMapSizeDialog() {
        return canvas.showScaleMapSizeDialog();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showSelectAmountDialog(net.sf.freecol.common.model.GoodsType, int, int, boolean)
     */
    @Override
    public int showSelectAmountDialog(GoodsType goodsType, int available,
                                      int defaultAmount, boolean needToPay) {
        return canvas.showSelectAmountDialog(goodsType, available,
                                             defaultAmount, needToPay);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showSelectTributeAmountDialog(net.sf.freecol.common.model.StringTemplate, int)
     */
    @Override
    public int showSelectTributeAmountDialog(StringTemplate question,
                                             int maximum) {
        return canvas.showSelectTributeAmountDialog(question, maximum);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showSelectDestinationDialog(net.sf.freecol.common.model.Unit)
     */
    @Override
    public Location showSelectDestinationDialog(Unit unit) {
        return canvas.showSelectDestinationDialog(unit);
    }

    /**
     * Show server list panel.
     *
     * @param serverList the server list
     */
    public void showServerListPanel(List<ServerInfo> serverList) {
        canvas.showServerListPanel(serverList);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showStartGamePanel(net.sf.freecol.common.model.Game, net.sf.freecol.common.model.Player, boolean)
     */
    @Override
    public void showStartGamePanel(Game game, Player player,
                                   boolean singlePlayerMode) {
        canvas.showStartGamePanel(game, player, singlePlayerMode);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showStatisticsPanel()
     */
    @Override
    public void showStatisticsPanel() {
        canvas.showStatisticsPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showStatusPanel(java.lang.String)
     */
    @Override
    public void showStatusPanel(String message) {
        canvas.showStatusPanel(message);
    }

    /**
     * Show tile panel.
     *
     * @param tile the tile
     */
    public void showTilePanel(Tile tile) {
        canvas.showTilePanel(tile);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showTilePopUpAtSelectedTile()
     */
    @Override
    public void showTilePopUpAtSelectedTile() {
        Tile tile = mapViewer.getSelectedTile();
        Point point = mapViewer.calculateTilePosition(tile);
        canvas.showTilePopup(tile, point.x+mapViewer.getTileWidth(), point.y);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showTradeRoutePanel(net.sf.freecol.common.model.Unit)
     */
    @Override
    public void showTradeRoutePanel(Unit unit) {
        canvas.showTradeRoutePanel(unit);
    }

    /**
     * Show trade route input panel.
     *
     * @param newRoute the new route
     * @param callBack the call back
     */
    public void showTradeRouteInputPanel(TradeRoute newRoute,
                                         Runnable callBack) {
        canvas.showTradeRouteInputPanel(newRoute, callBack);
    }

    /**
     * Show train panel.
     */
    public void showTrainPanel() {
        canvas.showTrainPanel();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#showVictoryDialog(net.sf.freecol.client.gui.DialogHandler)
     */
    @Override
    public void showVictoryDialog(DialogHandler<Boolean> handler) {
        canvas.showVictoryDialog(handler);
    }

    /**
     * Show warehouse dialog.
     *
     * @param colony the colony
     * @return true, if successful
     */
    public boolean showWarehouseDialog(Colony colony) {
        return canvas.showWarehouseDialog(colony);
    }

    /**
     * Show work production panel.
     *
     * @param unit the unit
     */
    public void showWorkProductionPanel(Unit unit) {
        canvas.showWorkProductionPanel(unit);
    }

    /**
     * Update european subpanels.
     */
    public void updateEuropeanSubpanels() {
        canvas.updateEuropeanSubpanels();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#updateGameOptions()
     */
    @Override
    public void updateGameOptions() {
        canvas.updateGameOptions();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#updateMapGeneratorOptions()
     */
    @Override
    public void updateMapGeneratorOptions() {
        canvas.updateMapGeneratorOptions();
    }

    // Trivial delegations to MapViewer

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#centerActiveUnit()
     */
    @Override
    public void centerActiveUnit() {
        mapViewer.centerActiveUnit();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#changeViewMode(int)
     */
    @Override
    public void changeViewMode(int newViewMode) {
        mapViewer.changeViewMode(newViewMode);
    }

    /**
     * Calculate unit label position in tile.
     *
     * @param labelWidth the label width
     * @param labelHeight the label height
     * @param tileP the tile P
     * @return the point
     */
    public Point calculateUnitLabelPositionInTile(int labelWidth,int labelHeight,
                                                  Point tileP) {
        return mapViewer.calculateUnitLabelPositionInTile(
            labelWidth, labelHeight, tileP);
    }

    /**
     * Execute with unit out for animation.
     *
     * @param unit the unit
     * @param sourceTile the source tile
     * @param r the r
     */
    public void executeWithUnitOutForAnimation(final Unit unit,
                                               final Tile sourceTile,
                                               final OutForAnimationCallback r) {
        mapViewer.executeWithUnitOutForAnimation(unit, sourceTile, r);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#getActiveUnit()
     */
    @Override
    public Unit getActiveUnit() {
        return mapViewer==null ? null : mapViewer.getActiveUnit();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#getFocus()
     */
    @Override
    public Tile getFocus() {
        return mapViewer.getFocus();
    }

    /**
     * Gets the map scale.
     *
     * @return the map scale
     */
    public float getMapScale() {
        return mapViewer.getImageLibrary().getScaleFactor();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#getSelectedTile()
     */
    @Override
    public Tile getSelectedTile() {
        return mapViewer.getSelectedTile();
    }

    /**
     * Gets the tile bounds.
     *
     * @param tile the tile
     * @return the tile bounds
     */
    public Rectangle getTileBounds(Tile tile) {
        return mapViewer.calculateTileBounds(tile);
    }

    /**
     * Gets the tile position.
     *
     * @param tile the tile
     * @return the tile position
     */
    public Point getTilePosition(Tile tile) {
        return mapViewer.calculateTilePosition(tile);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#getViewMode()
     */
    @Override
    public int getViewMode() {
        return mapViewer.getViewMode();
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#setFocus(net.sf.freecol.common.model.Tile)
     */
    @Override
    public void setFocus(Tile tileToFocus) {
        mapViewer.setFocus(tileToFocus);
        canvas.refresh();
    }

    /**
     * Sets the focus immediately.
     *
     * @param tileToFocus the new focus immediately
     */
    public void setFocusImmediately(Tile tileToFocus) {
        mapViewer.setFocus(tileToFocus);
        Dimension size = canvas.getSize();
        canvas.paintImmediately(0, 0, size.width, size.height);
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#setSelectedTile(net.sf.freecol.common.model.Tile)
     */
    @Override
    public boolean setSelectedTile(Tile newTileToSelect) {
        boolean result = mapViewer.setSelectedTile(newTileToSelect);
        updateMapControls();
        updateMenuBar();
        return result;
    }

    /* (non-Javadoc)
     * @see net.sf.freecol.client.gui.GUI#toggleViewMode()
     */
    @Override
    public void toggleViewMode() {
        mapViewer.toggleViewMode();
    }

    // Forwarding to tileViewer

    /**
     * Creates the tile image with overlay and forest.
     *
     * @param type the type
     * @param size the size
     * @return the buffered image
     */
    public static BufferedImage createTileImageWithOverlayAndForest(
            TileType type, Dimension size) {
        return TileViewer.createTileImageWithOverlayAndForest(type, size);
    }

    /**
     * Creates the tile image with beach border and items.
     *
     * @param tile the tile
     * @return the buffered image
     */
    public BufferedImage createTileImageWithBeachBorderAndItems(Tile tile) {
        return tileViewer.createTileImageWithBeachBorderAndItems(tile);
    }

    /**
     * Creates the tile image.
     *
     * @param tile the tile
     * @return the buffered image
     */
    public BufferedImage createTileImage(Tile tile) {
        return tileViewer.createTileImage(tile);
    }

    /**
     * Creates the colony tile image.
     *
     * @param tile the tile
     * @param colony the colony
     * @return the buffered image
     */
    public BufferedImage createColonyTileImage(Tile tile, Colony colony) {
        return tileViewer.createColonyTileImage(tile, colony);
    }

    /**
     * Display colony tiles.
     *
     * @param g the g
     * @param tiles the tiles
     * @param colony the colony
     */
    public void displayColonyTiles(Graphics2D g, Tile[][] tiles, Colony colony) {
        tileViewer.displayColonyTiles(g, tiles, colony);
    }

}
