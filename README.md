Плагин Jira для автоматической сборки релиза в гитлабе

Заводится issue с типом "Релиз", с ней связываются issue, которые необходимо добавить в релиз. 
В этих задачах должно быть кастомное поле, в котором указывается ссылка на мерж-реквесты в гитлабе.
В интерфейсе отобразится таблица c информацией об этих issue
![image](https://user-images.githubusercontent.com/6868954/186187153-cd52a8fd-4486-40df-9bc1-6cc24c484684.png)
Нажав на кнопку смержить ветки запустится процесс сбора релиза:
1) создание ветки релиза, название берется из summary issue (в примере это release1.0)
2) создание мерж-реквестов в данную ветку и их акцепт
Если происходит ошибка (кионфликты при сборке) ветка удаляются, так же закрываются созданные МР