
package com.dynast.civcraft.items.components;


public class RegisterItemComponentAsync implements Runnable {

	public ItemComponent component;
	public String name;
	public boolean register;
	
	public RegisterItemComponentAsync(ItemComponent itemComp, String name, boolean register) {
		this.component = itemComp;
		this.name = name;
		this.register = register;
	}
	
	@Override
	public void run() {
//		
//		if (register) {
//		ItemComponent.lock.lock();
//			try {
//				ArrayList<ItemComponent> components = ItemComponent.componentsByType.get(name);
//				
//				if (components == null) {
//					components = new ArrayList<ItemComponent>();
//				}
//			
//				components.add(component);
//				ItemComponent.componentsByType.put(name, components);
//			} finally {
//				ItemComponent.lock.unlock();
//			}		
//		} else {
//			ItemComponent.lock.lock();
//			try {
//				ArrayList<ItemComponent> components = ItemComponent.componentsByType.get(name);
//				
//				if (components == null) {
//					return;
//				}
//			
//				components.remove(component);
//				ItemComponent.componentsByType.put(name, components);
//			} finally {
//				ItemComponent.lock.unlock();
//			}
//		}	
	}

}
