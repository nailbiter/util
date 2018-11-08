package com.github.nailbiter.util.opts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.vandermeer.asciitable.AsciiTable;
import gnu.getopt.Getopt;
import util.TableBuilder;

public class Option {
	public static final String DEFARGNAME = "ARG";
	private char shortKey_;
	private ArgEnum hasArgument_;
	public static enum ArgEnum{
		HASARGUMENT, NOARGUMENT
	};
	private String expl_;
	public Option(char shortKey, ArgEnum hasArgument,String explanation){
		shortKey_ = shortKey;
		hasArgument_ = hasArgument;
		expl_ = explanation;
	}
	static String toString(List<Option> options) {
		StringBuilder sb = new StringBuilder();
		for(Option o : options)
			sb.append(o.shortKey_+((o.hasArgument_==ArgEnum.HASARGUMENT)?":":""));
		return sb.toString()+"h";
	}
	public static Map<Character,Object> processKeyArgs(String progName,String[] args,List<Option> opts){
		Getopt g = new Getopt(progName,args,Option.toString(opts));
		HashMap<Character,Object> res = new HashMap<Character,Object>();
		int c;
		while ((c = g.getopt()) != -1)
 	   	{
			boolean flag = false;
			if(c=='h') {
				System.out.format("help:\n");
				TableBuilder tb = new TableBuilder();
				for(Option opt : opts) {
					tb.addNewlineAndTokens(
							"-"+((opt.hasArgument_==ArgEnum.NOARGUMENT)?
									String.format("%c", opt.shortKey_):String.format("%c %s", opt.shortKey_,DEFARGNAME)),
							opt.expl_);
				}
				System.out.println(tb.toString());
				System.exit(0);
			}
			for(Option opt : opts) {
				if(opt.shortKey_==c) {
					if(opt.hasArgument_==ArgEnum.HASARGUMENT)
						res.put(opt.shortKey_, g.getOptarg());
					else
						res.put(opt.shortKey_, true);
					flag = true;
					break;
				}
			}
			if(flag)
				continue;
			if(c=='?')
				continue;
			System.out.print("getopt() returned " + c + "\n");
 	   	}
		return res;
	}
}
