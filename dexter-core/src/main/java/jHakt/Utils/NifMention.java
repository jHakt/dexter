package jHakt.Utils;

public class NifMention implements Comparable<NifMention>
{
	private String mention;
	private int start;
	private int end;
	
	public NifMention(String mention, int start, int end)
	{
		this.mention = mention;
		this.start = start;
		this.end = end;
	}

	public String getMention() { return mention; }
	
	public void setMention(String newMention)
	{
		this.mention = newMention;
	}
	
	public int getStart() { return start; }

	public int getEnd() { return end; }

	@Override
	public int compareTo(NifMention o) 
	{
		if(this.start < o.start)
			return -1;
		else if(this.start > o.start)
			return 1;
		
		return 0;
	}


}
