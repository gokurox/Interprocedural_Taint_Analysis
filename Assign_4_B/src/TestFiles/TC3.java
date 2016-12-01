package TestFiles;

public class TC3
{
	int func (int x)
	{
		int temp = 100;
		return (x + temp);
	}

	@SuppressWarnings("unused")
	int swappingAgain(int x)
	{
		if(x > 0)
			return 2;
		int y = func (x);
		return 10;
	}

	int foo (int play)
	{
		int beta = 0;
		int alpha = beta + func(1);
		beta = swappingAgain(alpha);
		int gamma = 0; 
		while(beta > 3)
		{
			gamma = func(play);
			beta = beta - swappingAgain(gamma);
		}
		return (beta+gamma);
	}

	public static void main (String[] args)
	{
		int play = Integer.parseInt(args[0]);
		TC3 s = new TC3();
		int x = 0;
		if(play > 0)
			x = s.foo(play);
		else
		{
			int y = s.swappingAgain(play);
			System.out.println(y);
		}
		System.out.println(x);
	}
}
