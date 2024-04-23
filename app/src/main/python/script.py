from newspaper import Article
def meta_description(url):
    article = Article(url)
    article.download()
    article.parse()
    sentence = article.text.split(".")
    return ". ".join(sentence[:10])
def title(url):
    article = Article(url)
    article.download()
    article.parse()
    return article.title
num = 5