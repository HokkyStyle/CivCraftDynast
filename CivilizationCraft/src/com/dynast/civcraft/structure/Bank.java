package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.dynast.civcraft.components.NonMemberFeeComponent;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.lorestorage.LoreMaterial;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Buff;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.StructureSign;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.util.SimpleBlock;
import org.bukkit.inventory.ItemStack;

public class Bank extends Structure {
	
	private int level = 1;
	private double interestRate = 0;
	
	private NonMemberFeeComponent nonMemberFeeComponent;

	private static final int IRON_SIGN = 0;
	private static final int GOLD_SIGN = 1;
	private static final int DIAMOND_SIGN = 2;
	private static final int EMERALD_SIGN = 3;
	private static final int IRON_BLOCK_SIGN = 4;
	private static final int GOLD_BLOCK_SIGN = 5;
	private static final int DIAMOND_BLOCK_SIGN = 6;
	private static final int EMERALD_BLOCK_SIGN = 7;
	
	protected Bank(Location center, String id, Town town) throws CivException {
		super(center, id, town);
		nonMemberFeeComponent = new NonMemberFeeComponent(this);
		nonMemberFeeComponent.onSave();
		setLevel(town.saved_bank_level);
		setInterestRate(town.saved_bank_interest_amount);
	}
	
	public Bank(ResultSet rs) throws SQLException, CivException {
		super(rs);
		nonMemberFeeComponent = new NonMemberFeeComponent(this);
		nonMemberFeeComponent.onLoad();
	}

	public double getBankExchangeRate() {
		double exchange_rate = 0.0;
		switch (level) {
		case 1:
			exchange_rate = 0.40;
			break;
		case 2:
			exchange_rate = 0.50;
			break;
		case 3:
			exchange_rate = 0.60;
			break;
		case 4:
			exchange_rate = 0.70;
			break;
		case 5:
			exchange_rate = 0.85;
			break;
		case 6:
			exchange_rate = 1;
			break;
		case 7:
			exchange_rate = 1.2;
			break;
		case 8:
			exchange_rate = 1.4;
			break;
		case 9:
			exchange_rate = 1.75;
			break;
		case 10:
			exchange_rate = 2;
			break;
		}

		if (this.getTown().getBuffManager().hasBuff(Buff.BARTER)) {
			exchange_rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.BARTER);
		}
		if (this.getTown().getBuffManager().hasBuff(Buff.BARTER_MINT)) {
			exchange_rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.BARTER_MINT);
		}

		if (this.getTown().getCiv().hasInstitution("freedom_3")) {
			exchange_rate += CivSettings.publicinsts.get("freedom_3").value;
		}

		return exchange_rate;
	}
	
	@Override
	public void onBonusGoodieUpdate() {
		this.updateSignText();
	}
	
	private String getExchangeRateString() {
		return (CivSettings.localize.localizedString("bank_exchange_rate"))+" "+((int)(getBankExchangeRate()*100) + "%");
	}

	private String getBuyRateString() {
		return (CivSettings.localize.localizedString("bank_exchange_rate"))+" "+((int)(CivGlobal.getMaxBankRate()*100) + "%");
	}
	
	public String getNonResidentFeeString() {
		return CivSettings.localize.localizedString("bank_sign_fee")+" "+(int)(this.nonMemberFeeComponent.getFeeRate() * 100) + "%";
	}
	
	private String getSignItemPrice(int signId) {
		double itemPrice;
		if (signId == IRON_SIGN) {
			itemPrice = CivSettings.iron_rate;
		} else if (signId == IRON_BLOCK_SIGN) {
			itemPrice = CivSettings.iron_rate*9;
		} else if (signId == GOLD_SIGN) {
			itemPrice = CivSettings.gold_rate;
		} else if (signId == GOLD_BLOCK_SIGN) {
			itemPrice = CivSettings.gold_rate*9;
		} else if (signId == DIAMOND_SIGN) {
			itemPrice = CivSettings.diamond_rate;
		} else if (signId == DIAMOND_BLOCK_SIGN) {
			itemPrice = CivSettings.diamond_rate*9;
		} else if (signId == EMERALD_SIGN) {
			itemPrice = CivSettings.emerald_rate;
		} else {
			itemPrice = CivSettings.emerald_rate*9;
		}
		
		String out = CivSettings.localize.localizedString("bank_price")+(int)(itemPrice*getBankExchangeRate())+"/"+(int)(itemPrice*9*getBankExchangeRate());

		return out;
	}

	private String getSignBlockPrice(int signId) {
		double itemPrice;
		if (signId == IRON_SIGN) {
			itemPrice = CivSettings.iron_rate;
		} else if (signId == IRON_BLOCK_SIGN) {
			itemPrice = CivSettings.iron_rate*9;
		} else if (signId == GOLD_SIGN) {
			itemPrice = CivSettings.gold_rate;
		} else if (signId == GOLD_BLOCK_SIGN) {
			itemPrice = CivSettings.gold_rate*9;
		} else if (signId == DIAMOND_SIGN) {
			itemPrice = CivSettings.diamond_rate;
		} else if (signId == DIAMOND_BLOCK_SIGN) {
			itemPrice = CivSettings.diamond_rate*9;
		} else if (signId == EMERALD_SIGN) {
			itemPrice = CivSettings.emerald_rate;
		} else {
			itemPrice = CivSettings.emerald_rate*9;
		}

		String out = CivSettings.localize.localizedString("bank_price")+(int)(itemPrice*CivGlobal.getMaxBankRate());

		return out;
	}
	
	private void exchange_for_coins(Resident resident, int itemId, double coins) throws CivException {
		double exchange_rate = 0.0;
		String itemName;
		Player player = CivGlobal.getPlayer(resident);
		
		if (itemId == CivData.IRON_INGOT || itemId == CivData.IRON_BLOCK)
			itemName = CivSettings.localize.localizedString("bank_itemName_iron");
		else if (itemId == CivData.GOLD_INGOT || itemId == CivData.GOLD_BLOCK)
			itemName = CivSettings.localize.localizedString("bank_itemName_gold");
		else if (itemId == CivData.DIAMOND || itemId == CivData.DIAMOND_BLOCK)
			itemName = CivSettings.localize.localizedString("bank_itemName_diamond");
		else if (itemId == CivData.EMERALD || itemId == CivData.EMERALD_BLOCK)
			itemName = CivSettings.localize.localizedString("bank_itemName_emerald");
		else 
			itemName = CivSettings.localize.localizedString("bank_itemName_stuff");
		
		exchange_rate = getBankExchangeRate();
		int count = resident.takeItemsInHand(itemId, 0);
		if (count == 0) {
			throw new CivException(CivSettings.localize.localizedString("var_bank_notEnoughInHand",itemName));
		}
		
		Civilization usersTown = resident.getCiv();
		
		// Resident is in his own town.
		if (usersTown == this.getCiv()) {		
			DecimalFormat df = new DecimalFormat();
			resident.getTreasury().deposit((double)((int)((coins*count)*exchange_rate)));
			CivMessage.send(player,
					CivColor.LightGreen + CivSettings.localize.localizedString("var_bank_exchanged",count,itemName,(df.format((coins*count)*exchange_rate)),CivSettings.CURRENCY_NAME));	
			return;
		}
		
		// non-resident must pay the town's non-resident tax
		double giveToPlayer = (double)((int)((coins*count)*exchange_rate));
		double giveToTown = (double)((int)giveToPlayer*this.getNonResidentFee());
		giveToPlayer -= giveToTown;
		
		giveToTown = Math.round(giveToTown);
		giveToPlayer = Math.round(giveToPlayer);
		
		this.getTown().depositDirect(giveToTown);
		resident.getTreasury().deposit(giveToPlayer);
		
		CivMessage.send(player, CivColor.LightGreen + CivSettings.localize.localizedString("var_bank_exchanged",count,itemName,giveToPlayer,CivSettings.CURRENCY_NAME));
		CivMessage.send(player,CivColor.Yellow+" "+CivSettings.localize.localizedString("var_taxes_paid",giveToTown,CivSettings.CURRENCY_NAME));
		
	}

	@SuppressWarnings("deprecation")

	private void buy_item(Resident resident, int itemId, double coins) throws CivException {
		double exchange_rate;
		String itemName;
		Player player = CivGlobal.getPlayer(resident);
		DecimalFormat df = new DecimalFormat();

		if (itemId == CivData.IRON_INGOT)
			itemName = CivSettings.localize.localizedString("bank_itemName_iron");
		else if (itemId == CivData.IRON_BLOCK)
			itemName = CivSettings.localize.localizedString("bank_itemName_ironBlock");
		else if (itemId == CivData.GOLD_INGOT)
			itemName = CivSettings.localize.localizedString("bank_itemName_gold");
		else if (itemId == CivData.GOLD_BLOCK)
			itemName = CivSettings.localize.localizedString("bank_itemName_goldBlock");
		else if (itemId == CivData.DIAMOND)
			itemName = CivSettings.localize.localizedString("bank_itemName_diamond");
		else if (itemId == CivData.DIAMOND_BLOCK)
			itemName = CivSettings.localize.localizedString("bank_itemName_diamondBlock");
		else if (itemId == CivData.EMERALD)
			itemName = CivSettings.localize.localizedString("bank_itemName_emerald");
		else if (itemId == CivData.EMERALD_BLOCK)
			itemName = CivSettings.localize.localizedString("bank_itemName_emeraldBlock");
		else
			itemName = CivSettings.localize.localizedString("bank_itemName_stuff");

		int coin = (int) coins;

		if (!resident.getTreasury().hasEnough(coin)) {
			throw new CivException(CivSettings.localize.localizedString("bank_notEnough_money"));
		}

		HashMap<Integer, ItemStack> stack = player.getInventory().addItem(new ItemStack(itemId, 1));
		resident.getTreasury().withdraw(coin);
		CivMessage.send(player,
				CivColor.LightGreen + CivSettings.localize.localizedString("var_bank_buyed",itemName,(df.format(coin)),CivSettings.CURRENCY_NAME));

		if (stack.size() > 0) {
			for (ItemStack stack1 : stack.values()) {
				player.getWorld().dropItem(player.getLocation(), stack1);
			}
		}
	}
	
	@Override
	public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
		//int special_id = Integer.valueOf(sign.getAction());
		Resident resident = CivGlobal.getResident(player);
		
		if (resident == null) {
			return;
		}
		
		try {
			ItemStack itemStack = player.getInventory().getItemInMainHand();
			if (LoreMaterial.isCustom(itemStack) || CivGlobal.isBonusGoodie(itemStack)) {
				throw new CivException(CivSettings.localize.localizedString("bank_invalidItem"));
			}
			
			switch (sign.getAction()) {
			case "iron":
				if (player.getInventory().getItemInMainHand().getType().equals(Material.IRON_INGOT)) {
					exchange_for_coins(resident, CivData.IRON_INGOT, CivSettings.iron_rate);
					break;
				} else if (player.getInventory().getItemInMainHand().getType().equals(Material.IRON_BLOCK)) {
					exchange_for_coins(resident, CivData.IRON_BLOCK, CivSettings.iron_rate * 9);
					break;
				}
				break;
			case "gold":
				if (player.getInventory().getItemInMainHand().getType().equals(Material.GOLD_INGOT)) {
					exchange_for_coins(resident, CivData.GOLD_INGOT, CivSettings.gold_rate);
					break;
				} else if (player.getInventory().getItemInMainHand().getType().equals(Material.GOLD_BLOCK)) {
					exchange_for_coins(resident, CivData.GOLD_BLOCK, CivSettings.gold_rate * 9);
					break;
				}
				break;
			case "diamond":
				if (player.getInventory().getItemInMainHand().getType().equals(Material.DIAMOND)) {
					exchange_for_coins(resident, CivData.DIAMOND, CivSettings.diamond_rate);
					break;
				} else if (player.getInventory().getItemInMainHand().getType().equals(Material.DIAMOND_BLOCK)) {
					exchange_for_coins(resident, CivData.DIAMOND_BLOCK, CivSettings.diamond_rate * 9);
					break;
				}
				break;
			case "emerald":
				if (player.getInventory().getItemInMainHand().getType().equals(Material.EMERALD)) {
					exchange_for_coins(resident, CivData.EMERALD, CivSettings.emerald_rate);
					break;
				} else if (player.getInventory().getItemInMainHand().getType().equals(Material.EMERALD_BLOCK)) {
					exchange_for_coins(resident, CivData.EMERALD_BLOCK, CivSettings.emerald_rate * 9);
					break;
				}
				break;
			case "ironb":
				buy_item(resident, CivData.IRON_BLOCK, CivSettings.iron_rate*9*CivGlobal.getMaxBankRate());
				break;
			case "goldb":
				buy_item(resident, CivData.GOLD_BLOCK, CivSettings.gold_rate*9*CivGlobal.getMaxBankRate());
				break;
			case "diamondb":
				buy_item(resident, CivData.DIAMOND_BLOCK, CivSettings.diamond_rate*9*CivGlobal.getMaxBankRate());
				break;
			case "emeraldb":
				buy_item(resident, CivData.EMERALD_BLOCK, CivSettings.emerald_rate*9*CivGlobal.getMaxBankRate());
				break;
			}
		} catch (CivException e) {
			CivMessage.send(player, CivColor.Rose+e.getMessage());
		}
	}
	
	@Override
	public void updateSignText() {
		for (StructureSign sign : getSigns()) {
			
			switch (sign.getAction().toLowerCase()) {
			case "iron":
				sign.setText(CivSettings.localize.localizedString("bank_sell_item")+CivSettings.localize.localizedString("bank_itemName_iron")+"\n"+
						getExchangeRateString()+"\n"+
						getSignItemPrice(IRON_SIGN)+"\n"+
						getNonResidentFeeString());
				break;
			case "gold":
				sign.setText(CivSettings.localize.localizedString("bank_sell_item")+CivSettings.localize.localizedString("bank_itemName_gold")+"\n"+
						getExchangeRateString()+"\n"+
						getSignItemPrice(GOLD_SIGN)+"\n"+
						getNonResidentFeeString());
				break;
			case "diamond":
				sign.setText(CivSettings.localize.localizedString("bank_sell_item")+CivSettings.localize.localizedString("bank_itemName_diamond")+"\n"+
						getExchangeRateString()+"\n"+
						getSignItemPrice(DIAMOND_SIGN)+"\n"+
						getNonResidentFeeString());
				break;			
			case "emerald":
					sign.setText(CivSettings.localize.localizedString("bank_sell_item")+CivSettings.localize.localizedString("bank_itemName_emerald")+"\n"+
							getExchangeRateString()+"\n"+
							getSignItemPrice(EMERALD_SIGN)+"\n"+
							getNonResidentFeeString());
					break;
			case "ironb":
				sign.setText(CivSettings.localize.localizedString("bank_buy_item")+CivSettings.localize.localizedString("bank_itemName_ironBlock")+"\n"+
						getBuyRateString()+"\n"+
						getSignBlockPrice(IRON_BLOCK_SIGN)+"\n"+
						getNonResidentFeeString());
				break;
			case "goldb":
				sign.setText(CivSettings.localize.localizedString("bank_buy_item")+CivSettings.localize.localizedString("bank_itemName_goldBlock")+"\n"+
						getBuyRateString()+"\n"+
						getSignBlockPrice(GOLD_BLOCK_SIGN)+"\n"+
						getNonResidentFeeString());
				break;
			case "diamondb":
				sign.setText(CivSettings.localize.localizedString("bank_buy_item")+CivSettings.localize.localizedString("bank_itemName_diamondBlock")+"\n"+
						getBuyRateString()+"\n"+
						getSignBlockPrice(DIAMOND_BLOCK_SIGN)+"\n"+
						getNonResidentFeeString());
				break;			
			case "emeraldb":
				sign.setText(CivSettings.localize.localizedString("bank_buy_item")+CivSettings.localize.localizedString("bank_itemName_emeraldBlock")+"\n"+
						getBuyRateString()+"\n"+
						getSignBlockPrice(EMERALD_BLOCK_SIGN)+"\n"+
						getNonResidentFeeString());
				break;
			}
				
			
			sign.update();
		}
	}

	@Override
	public String getDynmapDescription() {
		String out = "<u><b>"+CivSettings.localize.localizedString("bank_dynmapName")+"</u></b><br/>";
		out += CivSettings.localize.localizedString("Level")+" "+this.level;
		return out;
	}
	
	@Override
	public String getMarkerIconName() {
		return "bank";
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public double getNonResidentFee() {
		return this.nonMemberFeeComponent.getFeeRate();
	}

	public void setNonResidentFee(double nonResidentFee) {
		this.nonMemberFeeComponent.setFeeRate(nonResidentFee);
	}

	public double getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(double interestRate) {
		this.interestRate = interestRate;
	}
	
	@Override
	public void onLoad() {
		/* Process the interest rate. */
		if (interestRate == 0.0) {
			this.getTown().getTreasury().setPrincipalAmount(0);
			return;
		}
		
		/* Update the principal with the new value. */
		this.getTown().getTreasury().setPrincipalAmount(this.getTown().getTreasury().getBalance());
	}
	
	@Override
	public void onDailyEvent() {
		
		/* Process the interest rate. */
		if (interestRate == 0.0) {
			this.getTown().getTreasury().setPrincipalAmount(0);
			return;
		}
		
		double principal = this.getTown().getTreasury().getPrincipalAmount();
		
		if (this.getTown().getBuffManager().hasBuff("buff_greed_mint")) {
			double increase = this.getTown().getBuffManager().getEffectiveDouble("buff_greed_mint");
			interestRate *= increase;
			CivMessage.sendTown(this.getTown(), CivColor.LightGray+CivSettings.localize.localizedString("bank_greed_mint"));
		}
		
		if (this.getTown().getBuffManager().hasBuff("buff_greed")) {
			double increase = this.getTown().getBuffManager().getEffectiveDouble("buff_greed");
			interestRate *= increase;
			CivMessage.sendTown(this.getTown(), CivColor.LightGray+CivSettings.localize.localizedString("bank_greed"));
		}
		
		double newCoins = principal*interestRate;

		//Dont allow fractional coins.
		newCoins = Math.floor(newCoins);
		
		if (newCoins != 0) {
			CivMessage.sendTown(this.getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_bank_interestMsg1",newCoins,CivSettings.CURRENCY_NAME,principal));
			this.getTown().getTreasury().deposit(newCoins);
			
		}
		
		/* Update the principal with the new value. */
		this.getTown().getTreasury().setPrincipalAmount(this.getTown().getTreasury().getBalance());
		
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
		this.level = getTown().saved_bank_level;
		this.interestRate = getTown().saved_bank_interest_amount;
	}

	public NonMemberFeeComponent getNonMemberFeeComponent() {
		return nonMemberFeeComponent;
	}

	public void setNonMemberFeeComponent(NonMemberFeeComponent nonMemberFeeComponent) {
		this.nonMemberFeeComponent = nonMemberFeeComponent;
	}
	
	public void onGoodieFromFrame() {
		this.updateSignText();
	}

	public void onGoodieToFrame() {
		this.updateSignText();
	}
	
}
