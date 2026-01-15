package com.chernykh.moodmovieapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.chernykh.moodmovieapp.R;
import java.util.Arrays;
import java.util.List;

public class MoodTestFragment extends Fragment {

    private TextView tvQuestion, tvQuestionNumber;
    private Button btnOption1, btnOption2, btnOption3, btnOption4;

    private int currentQuestion = 0;
    private int moodScore = 0; // happy +1, neutral 0, sad -1

    private List<Question> questions = Arrays.asList(
            new Question("Как вы себя чувствуете сегодня?",
                    "😊 Отлично", "🙂 Хорошо", "😐 Нормально", "😔 Не очень"),
            new Question("Что бы вы хотели делать?",
                    "🎭 Развлекаться", "🎬 Смотреть кино", "📚 Отдохнуть", "💭 Подумать"),
            new Question("Какой ритм вам ближе?",
                    "💃 Быстрый", "🚶‍♂️ Средний", "🛋️ Медленный", "🌙 Спокойный"),
            new Question("Какие цвета вам нравятся?",
                    "🌈 Яркие", "🎨 Разноцветные", "⚫⚪ Нейтральные", "🌑 Тёмные"),
            new Question("Какой фильм посмотреть?",
                    "😂 Смешной", "🚀 Захватывающий", "🤔 Интересный", "😢 Душевный")
    );

    private OnMoodSelectedListener listener;

    public interface OnMoodSelectedListener {
        void onMoodSelected(String mood);
    }

    public void setOnMoodSelectedListener(OnMoodSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_test_new, container, false);

        tvQuestion = view.findViewById(R.id.tvQuestion);
        tvQuestionNumber = view.findViewById(R.id.tvQuestionNumber);
        btnOption1 = view.findViewById(R.id.btnOption1);
        btnOption2 = view.findViewById(R.id.btnOption2);
        btnOption3 = view.findViewById(R.id.btnOption3);
        btnOption4 = view.findViewById(R.id.btnOption4);

        showQuestion(currentQuestion);

        // Обработчики для 4 вариантов ответов
        btnOption1.setOnClickListener(v -> onAnswerSelected(2));  // happy
        btnOption2.setOnClickListener(v -> onAnswerSelected(1));  // slightly happy
        btnOption3.setOnClickListener(v -> onAnswerSelected(0));  // neutral
        btnOption4.setOnClickListener(v -> onAnswerSelected(-1)); // sad

        return view;
    }

    private void showQuestion(int questionIndex) {
        Question question = questions.get(questionIndex);

        tvQuestionNumber.setText("Вопрос " + (questionIndex + 1) + " из " + questions.size());
        tvQuestion.setText(question.text);
        btnOption1.setText(question.option1);
        btnOption2.setText(question.option2);
        btnOption3.setText(question.option3);
        btnOption4.setText(question.option4);
    }

    private void onAnswerSelected(int score) {
        moodScore += score;
        currentQuestion++;

        if (currentQuestion < questions.size()) {
            showQuestion(currentQuestion);
        } else {
            String mood = determineMood();
            if (listener != null) {
                listener.onMoodSelected(mood);
            }
        }
    }

    private String determineMood() {
        if (moodScore >= 6) {
            return "happy";
        } else if (moodScore >= 2) {
            return "happy";
        } else if (moodScore >= -1) {
            return "neutral";
        } else if (moodScore >= -4) {
            return "sad";
        } else {
            return "sad";
        }
    }

    private static class Question {
        String text;
        String option1, option2, option3, option4;

        Question(String text, String o1, String o2, String o3, String o4) {
            this.text = text;
            this.option1 = o1;
            this.option2 = o2;
            this.option3 = o3;
            this.option4 = o4;
        }
    }
}