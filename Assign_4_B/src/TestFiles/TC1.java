package TestFiles;

public class TC1
{
	public static int func (int x, int y)
	{
		int temp = 100;
		return (x + temp + y);
	}

	public static int swappingAgain (int x, int y, int q)
	{
		return (x + y + q); 
	}

	int foo (int play) 
	{
		return play*play;
	}

	public static void main (String[] args)
	{
		int play = Integer.parseInt(args[0]);
		TC1 s = new TC1();
		int x;
		int beta = 0;
		if(play > 0)
			x = s.foo(play);
		else
		{
			x = s.foo(4);
			System.out.println(x);
		}
		System.out.println(x);

		int alpha = beta + func(1,2);
		beta = swappingAgain(beta,alpha,beta);
		int gamma = 0; 
		while(beta > 3)
		{
			System.out.println(gamma);
			gamma = swappingAgain(1,2,play);
			beta = beta - func(beta,gamma);
		}
		System.out.println(beta);
	}
}