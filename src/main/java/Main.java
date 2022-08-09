public class Main {

    public static class Test{

        public int i = 0;

        public void run(Runnable runnable){
            runnable.run();
        }

        public void runOn(Test test){
            test.run(()->{
                System.out.println(i);
            });
        }
    }

    public static void main(String[] args) {
        Test test1 = new Test();
        Test test2 = new Test();
        test2.i = 3;

        test2.runOn(test1);
    }
}
