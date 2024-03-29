/**
 * SWEN30006 Software Modelling and Design
 * Semester 1, 2017
 * Project - Part A
 * 
 * Name: Emmanuel Macario
 * Student Number: 831659
 * Last Modified: 20/03/18
 * 
 */

/** Package Name. */
package strategies;


/** Useful classes from the Standard Java Library. */
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/** Importing classes from the package. */
import automail.*;
import exceptions.TubeFullException;



/**
 * The MyMailPool class contains a mail pool of items
 * to be delivered throughout different floors in a
 * building. Mail items are stored in an array list 
 * for easy access, and items are prioritised for delivery
 * by being sorted with respect to their estimated time 
 * measure scores. The class places mail items in a robot's 
 * tube in such a manner that increases efficiency in its 
 * total travel time.
 */
public class MyMailPool implements IMailPool {
    
    /**
     * The current pool of mail that needs
     * to be delivered.
     */
    private ArrayList<MailItem> itemPool;
    
    
    /** 
     * MyMailPool constructor.
     * @param void
     * @return New MyMailPool object.
     */
    public MyMailPool() {
        itemPool = new ArrayList<MailItem>();
    }
    

    /**
     * Adds an item to the mail pool
     * 
     * @param mailItem the mail item being added.
     * @return void
     */
    @Override
    public void addToPool(MailItem mailItem) {
        itemPool.add(mailItem);
    }
    
    
    /**
     * Fills the current storage tube with the item(s) that
     * have accumulated the highest 'measure' score thus far.
     * Only mail items with weight lower than the weight threshold 
     * for the particular robot are considered for filling the
     * storage tube.
     * 
     * @param tube refers to the pack the robot uses to deliver mail.
     * @param strong is whether the tube belongs to a strong robot.
     */
    @Override
    public void fillStorageTube(StorageTube tube, boolean strong) {
    	
    	/* Get the maximum weight limit for the robot.
    	 */
    	int weightLimit = getWeightLimit(strong);
    	
    	/* Calculate the total number of
    	 * free slots in the storage tube.
    	 */
    	int availableSlots = tube.MAXIMUM_CAPACITY - tube.getSize();
    	
    	
    	/* Get the 'best' items to add to the tube.
    	 */
    	ArrayList<MailItem> itemsToAdd = getBestMail(weightLimit, availableSlots);
    	
    	
    	/* Insert each item into the tube in order
    	 * of highest destination floor foremost.
    	 */
    	for (int i = itemsToAdd.size()-1; i >= 0; i--) {
            try {
                tube.addItem(itemsToAdd.get(i));
            } catch (TubeFullException e) {
                e.printStackTrace();
            }
        }
    }
    

    /**
     * Selects the 'best' possible items to be delivered by
     * the current robot. Does this by sorting the entire mail
     * pool by the estimated time measure score for each item, and
     * then choosing the items that have accumulated the highest
     * measure scores to be delivered first.
     * 
     * @param weightLimit    Maximum weight limit for a robot.
     * @param availableSlots Number of available slots in a robot's tube.
     * @return itemsToAdd	 The 'best' possible items for a robot to deliver.
     */
    public ArrayList<MailItem> getBestMail(int weightLimit, int availableSlots) {
    	
    	/* The items to add to a storage tube.
    	 */
    	ArrayList<MailItem> itemsToAdd = new ArrayList<>();
        
    	/* Objects to help with sorting the mail items.
    	 */
        MeasureComparator measureComparator = new MeasureComparator();
        FloorComparator floorComparator = new FloorComparator();
        
        /* Firstly, sort the mail by estimated measure values.
         */
        Collections.sort(itemPool, measureComparator);
        
        /* Then, for each available slot in the tube, try to add 
         * the 'best' possible mail item to the collection 
         * of items to be added to the tube. Ensure that
         * items to be added to the tube are removed from
         * the mail pool.
         */
        for (int i = 0; i < availableSlots; i++) {
        	
            MailItem itemToAdd = null;

            for (int j = itemPool.size()-1; j >= 0; j--) {
            	
                MailItem mailItem = itemPool.get(j);
                
                if (mailItem.getWeight() < weightLimit) {
                    itemToAdd = mailItem;
                    break;
                }
            }
            if (itemToAdd != null) {
                itemsToAdd.add(itemToAdd);
                itemPool.remove(itemToAdd);
            }
        }
        
        /* Once we have collected the 'best' possible
         * set of mail items to be added to the tube,
         * sort them in ascending order of destination
         * floor.
         */
        Collections.sort(itemsToAdd, floorComparator);
        
        return itemsToAdd;
    }
    
    
    /**
     * Calculates the estimated measure score of time taken 
     * to deliver an item. This is the same measure used
     * to judge the system's performance across all delivered
     * mailed items.
     * 
     * @param mailItem
     * @return An estimate of the measure score.
     */
    private double calculateMeasureScore(MailItem mailItem) {
        
    	/* The default priority for a normal item.
    	 */
        int priority = 0;
        
        /* Get priority level if the item is considered
         * to be priority mail.
         */
        if (mailItem instanceof PriorityMailItem) {
            priority = ((PriorityMailItem)mailItem).getPriorityLevel();
        }
        
        /* Estimate the total time between the arrival time of
         * the mail item and the delivery time, if it were to
         * be delivered immediately to the destination floor.
         * The +1 is the time it takes for a robot to unpack and
         * deliver an item, once it has reached the destination floor.
         */
        int estimatedTime = Clock.Time() - mailItem.getArrivalTime() 
        						+ mailItem.getDestFloor() + 1;
        
        /* Finally, calculate the estimate value for the measure.
         */
        return Math.pow(estimatedTime, 1.1) * (1 + Math.sqrt(priority));
    }
    
   
    /**
     * Gets the maximum weight limit (non-inclusive)
     * for a robot.
     * 
     * @param strong boolean indicating strong (true) or weak (false) robot.
     * @return weightLimit
     */
    private int getWeightLimit(boolean strong) {
    	return strong ? Integer.MAX_VALUE : 2000;
    }
    
    
    /** 
     * FloorComparator is a class used for comparing
     * two mail items according to their destination
     * floors. Higher destination floor items
     * are considered 'greater' than lower ones.
     */
    public class FloorComparator implements Comparator<MailItem> {
        @Override
        public int compare(MailItem itemOne, MailItem itemTwo) {
            return itemOne.getDestFloor() - itemTwo.getDestFloor();
        }
    }
    
    
    /**
     * MeasureComparator is a class used for comparing
     * two mail items according to the estimated measure
     * score for the items. Items with larger estimated measure 
     * scores are considered 'greater' than items with lower 
     * estimated measure scores.
     */
    public class MeasureComparator implements Comparator<MailItem> {
        @Override
        public int compare(MailItem itemOne, MailItem itemTwo) {
            return Double.compare(calculateMeasureScore(itemOne), 
            		calculateMeasureScore(itemTwo));
        }
    }
}