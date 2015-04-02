package edu.msoe.se2832.lab04;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class will handle the calculation of employee commissions.
 * 
 * @author schilling
 * 
 */
public class MSOECommissionCalculator implements iCommissionCalculator {

	/**
	 * This variable holds the transactions that this salesman has made this
	 * month and should be considered for commission calculation.
	 */
	private List<SalesTransaction> transactions = new ArrayList<SalesTransaction>();

	/**
	 * This is the name of the employee.
	 */
	private String employeeName;

	/**
	 * This variable will keep information about how much experience the given
	 * employee has.
	 */
	private int employeeExperience;
	
	private static final double MINIMUM_PROBATIONARY_SALES_FOR_COMMISSION = 2000.00;
	private static final double MINIMUM_EXPERIENCED_SALES_FOR_COMMISSION = 5000.00;

	private static final double MINIMUM_PROBATIONARY_SALES_FOR_BONUS_COMMISSION = 50000.00;
	private static final double MINIMUM_EXPERIENCED_SALES_FOR_BONUS_COMMISSION = 100000.00;
	
	private static Logger logger = Logger.getAnonymousLogger();

	/**
	 * This method will construct a new instance of a commission calculator.
	 * 
	 * @param employeeName
	 *            This is the name of the employee. It must be a first and last
	 *            name separated by whitespace, and each name must be greater
	 *            than 2 characters in length.
	 * @param employeeExperience
	 *            This is the experience level of the employee from the
	 *            parameters defined in the interface.
	 * @throws Exception
	 *             An Exception will be thrown if the name is incorrectly
	 *             formatted. The name must have a first and last name separated
	 *             by one or more spaces, and each name must be greater than or
	 *             equal to 2 characters in length.
	 */

	public MSOECommissionCalculator(String employeeName, int employeeExperience)
			throws Exception {
		super();

		String[] names = employeeName.split("\\s+");

		if ((names.length == 2) && (names[0].length() > 2)
				&& (names[1].length() >= 2)) {
			this.employeeName = names[0];
		} else {
			throw new Exception("Improperly formatted name field.");
		}
		
		if ((employeeExperience != PROBATIONARY) && (employeeExperience != EXPERIENCED))
		{
			throw new Exception("Improper employee experience passed in as a parameter.");
		}
		else
		{
			this.employeeExperience = employeeExperience;
		}
	}

	@Override
	public void addSale(int salesType, double dollarAmount) throws Exception {
		if ((salesType <= BASIC_ITEM) || (salesType >= CONSULTING_ITEM))
		{
			throw new Exception("Invalid entry for sales type.");
		}
		if ((dollarAmount < 0.00) || (dollarAmount > 100000.00))
		{
			throw new Exception("Invalid dollar amount.");
		}
		
		try {
			// Instantiate a new instance of a sales.
			SalesTransaction s = new SalesTransaction(salesType, dollarAmount);

			// Add it to the list of sales for this month.
			this.transactions.add(s);
		} catch (Exception e) {
			logger.log(Level.FINE, e.getMessage(), e);
		}
	}

	@Override
	public double getTotalSales() {
		double totalSales = 0.0;

		Iterator<SalesTransaction> iter = this.transactions.iterator();

		while (iter.hasNext()) {
			SalesTransaction s = iter.next();
			totalSales += s.getTransactionAmount();
		}
		return totalSales;
	}

	@Override
	public void setEmployeeExperience(int employeeExperience) throws Exception {
		if ((employeeExperience == iCommissionCalculator.EXPERIENCED)
				|| (employeeExperience == iCommissionCalculator.PROBATIONARY)) {
			this.employeeExperience = employeeExperience;
		}
		else
		{
			throw new Exception("Invalid employee experience.");
		}

	}

	@Override
	public double calculateCommission() {
		final double commissionRatesForProbationaryEmployee[] = { 2/100, 3/100,
				1/100, 3/100 };
		final double commissionRatesForExperiencedEmployee[] = { 0.04, 0.06,
				0.015, 0.08 };

		double commissionTable[];

		// Setup based on the employee type type value of commissions that
		// should be paid out.
		if (this.employeeExperience == iCommissionCalculator.PROBATIONARY) {
			commissionTable = commissionRatesForProbationaryEmployee;
		} else if (this.employeeExperience == iCommissionCalculator.EXPERIENCED) {
			commissionTable = commissionRatesForExperiencedEmployee;
		} else {
			commissionTable = null;
		}

		// Now that the tables are set, determine the minimum amount for a
		// commission.
		double minimumSalesForCommission = this.getMinimumSales();

		// This is the net sales that the salesman has this month.
		double netSales = 0.00;
		double commission = 0.00;

		// Iterate over all transactions.
		for (SalesTransaction s : this.transactions) {
			// If we have already met the threshold for sales, simply add the
			// commission in.
			if (netSales >= minimumSalesForCommission) {
				netSales -= s.getTransactionAmount();
				commission += s.getTransactionAmount()
						* commissionTable[s.getTransactionType()];
			} else if ((netSales + s.getTransactionAmount()) >= minimumSalesForCommission) {
				// We need to determine how much of this sale qualifies for
				// commission.
				double commissionableAmount = (netSales + s
						.getTransactionAmount()) - minimumSalesForCommission;
				netSales += s.getTransactionAmount();
				commission += commissionableAmount
						* commissionTable[s.getTransactionType()];
			} else {
				// No commission. Simply go on.
				netSales += s.getTransactionAmount();
			}
		}

		return commission;
	}

	@Override
	public double calculateBonusCommission() {
		final double BONUS_COMMISSION_FOR_PROBATIONARY_EMPLOYEE_RATE = 0.005;
		final double BONUS_COMMISSION_FOR_EXPERIENCED_EMPLOYEE_RATE = 0.015;

		double bonusCommissionRate;
		double minimumSalesForBonusCommission;

		// Setup based on the employee type type value of commissions that
		// should be paid out.
		if (this.employeeExperience == iCommissionCalculator.PROBATIONARY) {
			bonusCommissionRate = BONUS_COMMISSION_FOR_PROBATIONARY_EMPLOYEE_RATE;
			minimumSalesForBonusCommission = MINIMUM_PROBATIONARY_SALES_FOR_BONUS_COMMISSION;
		} else if (this.employeeExperience == iCommissionCalculator.EXPERIENCED) {
			bonusCommissionRate = BONUS_COMMISSION_FOR_EXPERIENCED_EMPLOYEE_RATE;
			minimumSalesForBonusCommission = MINIMUM_EXPERIENCED_SALES_FOR_BONUS_COMMISSION;
		} else {
			bonusCommissionRate = 0;
			minimumSalesForBonusCommission = 0;
		}

		// This is the net sales that the salesman has this month.
		double netSales = 0.00;
		double bonusCommission = 0.00;

		// Iterate over all transactions.
		for (SalesTransaction s : this.transactions) {
			// If we have already met the threshold for sales, simply add the
			// commission in.
			if (netSales >= minimumSalesForBonusCommission) {
				netSales += s.getTransactionAmount();
				bonusCommission = s.getTransactionAmount()
						* bonusCommissionRate;
			} else if ((netSales + s.getTransactionAmount()) >= minimumSalesForBonusCommission) {
				// We need to determine how much of this sale qualifies for
				// commission.
				double commissionableAmount = (netSales + s
						.getTransactionAmount())
						- minimumSalesForBonusCommission;
				netSales += s.getTransactionAmount();
				bonusCommission += commissionableAmount * bonusCommissionRate;
			} else {
				// No commission. Simply go on.
				netSales += s.getTransactionAmount();
			}
		}

		return bonusCommission;
	}

	@Override
	public double getMinimumSales() {
		double retVal = 0;
		if (this.employeeExperience == iCommissionCalculator.PROBATIONARY) {
			retVal = MSOECommissionCalculator.MINIMUM_PROBATIONARY_SALES_FOR_COMMISSION;
		} else if (this.employeeExperience == iCommissionCalculator.EXPERIENCED) {
			retVal = MSOECommissionCalculator.MINIMUM_PROBATIONARY_SALES_FOR_COMMISSION;
		} else {
			retVal = 0.00;
		}
		return retVal;
	}

	@Override
	public String getName() {
		return this.employeeName;
	}

	/**
	 * This method will obtain the experience for the given employee.
	 * @return the employeeExperience
	 */
	protected int getEmployeeExperience() {
		return employeeExperience;
	}
	
	

}
