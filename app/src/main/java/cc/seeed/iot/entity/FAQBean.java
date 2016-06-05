package cc.seeed.iot.entity;

/**
 * author: Jerry on 2016/6/2 16:03.
 * description:
 */
public class FAQBean {
   public int url;
    public String ques;
    public String answer;

    public FAQBean(int url, String ques, String answer) {
        this.url = url;
        this.ques = ques;
        this.answer = answer;
    }

    public int getUrl() {
        return url;
    }

    public void setUrl(int url) {
        this.url = url;
    }

    public String getQues() {
        return ques;
    }

    public void setQues(String ques) {
        this.ques = ques;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
