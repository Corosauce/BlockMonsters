package blockmonsters.client.gui;

import blockmonsters.input.ControlInputs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSmallButton;
import net.minecraft.client.settings.KeyBinding;

public class GuiControlsPages extends GuiScreen
{
    /**
     * A reference to the screen object that created this. Used for navigating between screens.
     */
    private GuiScreen parentScreen;

    /** The title string that is displayed in the top-center of the screen. */
    protected String screenTitle = "Controls";

    /** Reference to the GameSettings object. */
    //private GameSettings options;
    /** The ID of the  button that has been pressed. */
    private int buttonId = -1;
    private int buttonIndex = -1;
    
    //New vars
    public int page = 0;
    public int pages = 0;
    public int elementsPerPage = 12;
    public int elementsThisPage;
    
    public int elementStart = 0;
    public int elementEnd = 0;

    public GuiControlsPages(GuiScreen par1GuiScreen, int page) {
    	this(par1GuiScreen);
    	this.page = page;  
    }
    
    public GuiControlsPages(GuiScreen par1GuiScreen)
    {
        this.parentScreen = par1GuiScreen;
        
        //height = 80;
    }

    private int func_20080_j()
    {
        return this.width / 2 - 155;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        int var2 = this.func_20080_j();

        elementStart = page * elementsPerPage;
        elementEnd = (page+1) * elementsPerPage;
        
        boolean hitEnd = false;
        
        int realSize = 0;
        for (int i = 0; i < ControlInputs.keyBindings.size(); i++) {
        	if (!ControlInputs.keyBindings.get(i).isRewireKey) realSize++;
        }
        
        if (elementEnd > realSize) {
        	elementEnd = realSize;
        	hitEnd = true;
        }
        elementsThisPage = elementEnd - elementStart;
        
        pages = (int)(realSize / elementsPerPage);
        
        screenTitle = "Controls - " + (page+1) + "/" + (pages+1);
        
        int counter = 0;
        
        
        
        for (int var3 = elementStart; var3 < elementEnd; ++var3)
        {
            this.buttonList.add(new GuiSmallButton(var3, var2 + counter % 2 * 160, this.height / 6 + 24 * (counter >> 1), 70, 20, ControlInputs.getKeyName(var3)));
            counter++;
        }

        this.buttonList.add(new GuiButton(201, this.width / 2 - 35 - 50 - 70, this.height / 6 + 168, 70, 20, page > 0 ? "Prev Page" : "\u00a77Prev Page"));
        this.buttonList.add(new GuiButton(202, this.width / 2 + 35 + 50, this.height / 6 + 168, 70, 20, !hitEnd ? "Next Page" : "\u00a77Next Page"));
        
        this.buttonList.add(new GuiButton(200, this.width / 2 - 35, this.height / 6 + 168, 70, 20, "Done"));
        //this.screenTitle = var1.translateKey("controls.title");
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        for (int var2 = 0; var2 < elementsThisPage; ++var2)
        {
            ((GuiButton)this.buttonList.get(var2)).displayString = ControlInputs.getKeyName((page*elementsPerPage)+var2);
        }
        
        if (par1GuiButton.id == 200)
        {
            this.mc.displayGuiScreen(this.parentScreen);
        }
        else if (par1GuiButton.id == 201)
        {
        	if (page > 0) page--;
            //this.mc.displayGuiScreen(this.parentScreen);
        	Minecraft.getMinecraft().displayGuiScreen(new GuiControlsPages(null, page));
        }
        else if (par1GuiButton.id == 202)
        {
        	if (page < pages) page++;
        	Minecraft.getMinecraft().displayGuiScreen(new GuiControlsPages(null, page));
            //this.mc.displayGuiScreen(this.parentScreen);
        }
        else
        {
            this.buttonId = /*(page*elementsPerPage)+*/par1GuiButton.id;
            this.buttonIndex = par1GuiButton.id - (page*elementsPerPage);
            par1GuiButton.displayString = "> " + ControlInputs.getKeyName(par1GuiButton.id) + " <";
        }
    }

    /**
     * Called when the mouse is clicked.
     */
    protected void mouseClicked(int par1, int par2, int par3)
    {
        if (this.buttonId >= 0)
        {
            ControlInputs.setKey(this.buttonId, -100 + par3);
            ((GuiButton)this.buttonList.get(this.buttonIndex)).displayString = ControlInputs.getKeyName(this.buttonId);
            this.buttonId = -1;
            KeyBinding.resetKeyBindingArrayAndHash();
        }
        else
        {
            super.mouseClicked(par1, par2, par3);
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char par1, int par2)
    {
        if (this.buttonId >= 0)
        {
        	ControlInputs.setKey(this.buttonId, par2);
            ((GuiButton)this.buttonList.get(this.buttonIndex)).displayString = ControlInputs.getKeyName(this.buttonId);
            this.buttonId = -1;
            KeyBinding.resetKeyBindingArrayAndHash();
        }
        else
        {
            super.keyTyped(par1, par2);
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 20, 16777215);
        int var4 = this.func_20080_j();

        int counter = 0;
        
        for (int var5 = elementStart; var5 < elementEnd; ++var5)
        {
            boolean var6 = false;

            for (int var7 = elementStart; var7 < elementEnd; ++var7)
            {
                if (var7 != var5 && ControlInputs.keyBindings.get(var5).keyCode == ControlInputs.keyBindings.get(var7).keyCode)
                {
                    var6 = true;
                }
            }

            if (this.buttonId == var5)
            {
                ((GuiButton)this.buttonList.get(counter)).displayString = "\u00a7f> \u00a7e??? \u00a7f<";
            }
            else if (var6)
            {
                ((GuiButton)this.buttonList.get(counter)).displayString = "\u00a7c" + ControlInputs.getKeyName(var5);
            }
            else
            {
            	try {
            		((GuiButton)this.buttonList.get(counter)).displayString = ControlInputs.getKeyName(var5);
            	} catch (Exception ex) {
            		ex.printStackTrace();
            	}
            }

            this.drawString(this.fontRenderer, ControlInputs.getKeyDescription(var5), var4 + counter % 2 * 160 + 70 + 6, this.height / 6 + 24 * (counter >> 1) + 7, -1);
            
            counter++;
        }

        super.drawScreen(par1, par2, par3);
    }
}
