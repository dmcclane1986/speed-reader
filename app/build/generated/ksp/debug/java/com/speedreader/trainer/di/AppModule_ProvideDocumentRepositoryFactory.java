package com.speedreader.trainer.di;

import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.speedreader.trainer.data.repository.DocumentRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AppModule_ProvideDocumentRepositoryFactory implements Factory<DocumentRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<FirebaseAuth> firebaseAuthProvider;

  public AppModule_ProvideDocumentRepositoryFactory(Provider<Context> contextProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<FirebaseAuth> firebaseAuthProvider) {
    this.contextProvider = contextProvider;
    this.firestoreProvider = firestoreProvider;
    this.firebaseAuthProvider = firebaseAuthProvider;
  }

  @Override
  public DocumentRepository get() {
    return provideDocumentRepository(contextProvider.get(), firestoreProvider.get(), firebaseAuthProvider.get());
  }

  public static AppModule_ProvideDocumentRepositoryFactory create(Provider<Context> contextProvider,
      Provider<FirebaseFirestore> firestoreProvider, Provider<FirebaseAuth> firebaseAuthProvider) {
    return new AppModule_ProvideDocumentRepositoryFactory(contextProvider, firestoreProvider, firebaseAuthProvider);
  }

  public static DocumentRepository provideDocumentRepository(Context context,
      FirebaseFirestore firestore, FirebaseAuth firebaseAuth) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDocumentRepository(context, firestore, firebaseAuth));
  }
}
